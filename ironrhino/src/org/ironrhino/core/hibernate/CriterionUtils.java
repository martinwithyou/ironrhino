package org.ironrhino.core.hibernate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.annotations.NaturalId;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.AnnotationShadows.UiConfigImpl;
import org.ironrhino.core.struts.EntityClassHelper;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.core.util.ApplicationContextUtils;
import org.ironrhino.core.util.CodecUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;

public class CriterionUtils {

	public static final String CRITERION_OPERATOR_SUFFIX = "-op";

	public static Criterion like(String value, MatchMode mode, String... names) {
		Criterion c = null;
		int index = value.indexOf(':');
		String field = null;
		if (index > 0) {
			field = value.substring(0, index);
			value = value.substring(index + 1);
			for (String name : names) {
				if (name.equals(field) || name.equals(field + "AsString")) {
					if (field.equals("tags")) {
						c = matchTag(name, value);
					} else {
						c = Restrictions.like(name, value, MatchMode.ANYWHERE);
					}
					break;
				}
			}
		} else {
			for (String name : names) {
				if (c == null) {
					c = Restrictions.like(name, value, MatchMode.ANYWHERE);
				} else {
					c = Restrictions.or(c,
							Restrictions.like(name, value, MatchMode.ANYWHERE));
				}
			}
		}
		return c;
	}

	public static Criterion matchTag(String tagFieldName, String tag) {
		tag = tag.trim();
		return Restrictions.or(Restrictions.eq(tagFieldName, tag),
				Restrictions.or(Restrictions.like(tagFieldName, tag + ",",
						MatchMode.START), Restrictions.or(Restrictions.like(
						tagFieldName, "," + tag, MatchMode.END),
						Restrictions.like(tagFieldName, "," + tag + ",",
								MatchMode.ANYWHERE))));
	}

	public static void filter(DetachedCriteria dc,
			Class<? extends Persistable<?>> entityClass) {
		filter(dc, entityClass, EntityClassHelper.getUiConfigs(entityClass));
	}

	public static void filter(DetachedCriteria dc,
			Class<? extends Persistable<?>> entityClass,
			Map<String, UiConfigImpl> uiConfigs) {
		if (dc == null || entityClass == null || uiConfigs == null)
			return;
		try {
			ConversionService conversionService = ApplicationContextUtils
					.getBean(ConversionService.class);
			Map<String, String> aliases = new HashMap<String, String>();
			Set<String> propertyNames = uiConfigs.keySet();
			BeanWrapperImpl bw = new BeanWrapperImpl(entityClass.newInstance());
			bw.setConversionService(conversionService);
			String entityName = StringUtils.uncapitalize(entityClass
					.getSimpleName());
			Map<String, String[]> parameterMap = ServletActionContext
					.getRequest().getParameterMap();
			for (String parameterName : parameterMap.keySet()) {
				String propertyName;
				String[] parameterValues;
				Object value1 = null;
				Object value2 = null;
				String operatorValue;
				if (parameterName.endsWith(CRITERION_OPERATOR_SUFFIX)) {
					propertyName = parameterName.substring(
							0,
							parameterName.length()
									- CRITERION_OPERATOR_SUFFIX.length());
					if (parameterMap.containsKey(propertyName))
						continue;
					parameterValues = new String[0];
					operatorValue = parameterMap.get(parameterName)[0];
				} else {
					propertyName = parameterName;
					parameterValues = parameterMap.get(parameterName);
					operatorValue = ServletActionContext.getRequest()
							.getParameter(
									parameterName + CRITERION_OPERATOR_SUFFIX);
				}
				if (propertyName.startsWith(entityName + "."))
					propertyName = propertyName.substring(propertyName
							.indexOf('.') + 1);
				String s = propertyName;
				if (s.indexOf('.') > 0)
					s = s.substring(0, s.indexOf('.'));
				UiConfigImpl config = uiConfigs.get(s);
				if (config == null || config.isExcludedFromCriteria())
					continue;
				CriterionOperator operator = null;
				if (StringUtils.isNotBlank(operatorValue))
					operator = CriterionOperator.valueOf(operatorValue
							.toUpperCase());
				if (operator == null)
					operator = CriterionOperator.EQ;
				if (parameterValues.length < operator.getParametersSize())
					continue;
				if (propertyName.indexOf('.') > 0) {
					String subPropertyName = propertyName
							.substring(propertyName.indexOf('.') + 1);
					propertyName = propertyName.substring(0,
							propertyName.indexOf('.'));
					if (propertyNames.contains(propertyName)) {
						Class<?> type = bw.getPropertyType(propertyName);
						if (Persistable.class.isAssignableFrom(type)) {
							BeanWrapperImpl bw2 = new BeanWrapperImpl(
									type.newInstance());
							bw2.setConversionService(conversionService);
							if (!operator
									.isEffective(
											bw2.getPropertyType(subPropertyName),
											parameterValues.length > 0 ? parameterValues[0]
													: null,
											parameterValues.length > 1 ? parameterValues[1]
													: null))
								continue;
							if (parameterValues.length > 0) {
								bw2.setPropertyValue(subPropertyName,
										parameterValues[0]);
								value1 = bw2.getPropertyValue(subPropertyName);
							}
							if (parameterValues.length > 1) {
								bw2.setPropertyValue(subPropertyName,
										parameterValues[1]);
								value2 = bw2.getPropertyValue(subPropertyName);
							}
							String alias = aliases.get(propertyName);
							if (alias == null) {
								alias = CodecUtils.randomString(4);
								dc.createAlias(propertyName, alias);
								aliases.put(propertyName, alias);
							}
							Criterion criterion = operator.operator(alias + "."
									+ subPropertyName, value1, value2);
							if (criterion != null)
								dc.add(criterion);
						}
					}
				} else if (propertyNames.contains(propertyName)) {
					Class<?> type = bw.getPropertyType(propertyName);
					if (Persistable.class.isAssignableFrom(type)) {
						@SuppressWarnings("unchecked")
						BaseManager<?> em = ApplicationContextUtils
								.getEntityManager((Class<? extends Persistable<?>>) type);
						try {
							BeanWrapperImpl bw2 = new BeanWrapperImpl(type);
							bw2.setConversionService(conversionService);
							bw2.setPropertyValue("id", parameterValues[0]);
							Persistable<?> p = em.get((Serializable) bw2
									.getPropertyValue("id"));
							if (p == null) {
								Map<String, NaturalId> naturalIds = AnnotationUtils
										.getAnnotatedPropertyNameAndAnnotations(
												type, NaturalId.class);
								if (naturalIds.size() == 1) {
									String name = naturalIds.entrySet()
											.iterator().next().getKey();
									bw2.setPropertyValue(name,
											parameterValues[0]);
									p = em.findOne((Serializable) bw2
											.getPropertyValue(name));
								}
							}
							if (p != null)
								dc.add(Restrictions.eq(propertyName, p));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						if (!operator.isEffective(type,
								parameterValues.length > 0 ? parameterValues[0]
										: null,
								parameterValues.length > 1 ? parameterValues[1]
										: null))
							continue;
						if (parameterValues.length > 0) {
							bw.setPropertyValue(propertyName,
									parameterValues[0]);
							value1 = bw.getPropertyValue(propertyName);
						}
						if (parameterValues.length > 1) {
							bw.setPropertyValue(propertyName,
									parameterValues[1]);
							value2 = bw.getPropertyValue(propertyName);
						}
						Criterion criterion = operator.operator(propertyName,
								value1, value2);
						if (criterion != null)
							dc.add(criterion);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
