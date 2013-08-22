package org.ironrhino.core.struts;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.hibernate.annotations.NaturalId;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.hibernate.CriterionOperator;
import org.ironrhino.core.hibernate.CriterionUtils;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.CaseInsensitive;
import org.ironrhino.core.metadata.Hidden;
import org.ironrhino.core.metadata.Owner;
import org.ironrhino.core.metadata.Readonly;
import org.ironrhino.core.metadata.Richtable;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.Enableable;
import org.ironrhino.core.model.Ordered;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.model.Tuple;
import org.ironrhino.core.search.SearchService.Mapper;
import org.ironrhino.core.search.elasticsearch.ElasticSearchCriteria;
import org.ironrhino.core.search.elasticsearch.ElasticSearchService;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableId;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.core.util.ApplicationContextUtils;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.core.type.TypeReference;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;

import freemarker.template.Template;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EntityAction<EN extends Persistable<?>> extends BaseAction {

	private static final long serialVersionUID = -8442983706126047413L;

	public static final String CRITERION_OPERATOR_SUFFIX = "-op";

	protected static Logger log = LoggerFactory.getLogger(EntityAction.class);

	private ReadonlyImpl readonly;

	private RichtableImpl richtableConfig;

	private Map<String, UiConfigImpl> uiConfigs;

	private Map<String, UiConfigImpl> propertyNamesInCriterion;

	protected ResultPage resultPage;

	private Persistable entity;

	private String entityName;

	private Map<String, NaturalId> naturalIds;

	@Autowired(required = false)
	private transient ElasticSearchService<Persistable<?>> elasticSearchService;

	@Autowired(required = false)
	private transient ConversionService conversionService;

	public boolean isSearchable() {
		if (getEntityClass().getAnnotation(Searchable.class) != null)
			return true;
		RichtableImpl rc = getRichtableConfig();
		boolean searchable = rc.isSearchable();
		if (searchable)
			return true;
		else
			for (Map.Entry<String, UiConfigImpl> entry : getUiConfigs()
					.entrySet())
				if (entry.getValue().isSearchable())
					return true;
		return false;
	}

	public boolean isEnableable() {
		return Enableable.class.isAssignableFrom(getEntityClass());
	}

	public Persistable getEntity() {
		return entity;
	}

	public ResultPage getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage resultPage) {
		this.resultPage = resultPage;
	}

	public RichtableImpl getRichtableConfig() {
		if (richtableConfig == null) {
			Richtable rc = getClass().getAnnotation(Richtable.class);
			if (rc == null)
				rc = getEntityClass().getAnnotation(Richtable.class);
			richtableConfig = new RichtableImpl(rc);
		}
		return richtableConfig;
	}

	public ReadonlyImpl getReadonly() {
		if (readonly == null) {
			Richtable rconfig = getClass().getAnnotation(Richtable.class);
			if (rconfig == null)
				rconfig = getEntityClass().getAnnotation(Richtable.class);
			Readonly rc = null;
			if (rconfig != null)
				rc = rconfig.readonly();
			Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();
			if (rc == null && ownerProperty != null) {
				Owner owner = ownerProperty.getKey();
				if (!owner.isolate()
						&& owner.readonlyForOther()
						&& !(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
								.authorize(null, owner.supervisorRole(), null))) {
					readonly = new ReadonlyImpl();
					readonly.setValue(false);
					StringBuilder sb = new StringBuilder("!entity.");
					sb.append(ownerProperty.getKey().propertyName())
							.append("?? || entity.")
							.append(ownerProperty.getKey().propertyName())
							.append("!=authentication('principal')");
					String expression = sb.toString();
					readonly.setExpression(expression);
					readonly.setDeletable(false);
				}
			}
			if (readonly == null)
				readonly = new ReadonlyImpl(rc);
		}
		return readonly;
	}

	// need call once before view
	public String getEntityName() {
		if (entityName == null)
			entityName = ActionContext.getContext().getActionInvocation()
					.getProxy().getActionName();
		return entityName;
	}

	public Map<String, NaturalId> getNaturalIds() {
		if (naturalIds != null)
			return naturalIds;
		naturalIds = AnnotationUtils.getAnnotatedPropertyNameAndAnnotations(
				getEntityClass(), NaturalId.class);
		return naturalIds;
	}

	public boolean isNaturalIdMutable() {
		return getNaturalIds().size() > 0
				&& ((NaturalId) getNaturalIds().values().iterator().next())
						.mutable();
	}

	public Map<String, UiConfigImpl> getUiConfigs() {
		if (uiConfigs == null) {
			Class clazz = getEntityClass();
			Set<String> hides = new HashSet<String>();
			final Map<String, UiConfigImpl> map = new HashMap<String, UiConfigImpl>();
			PropertyDescriptor[] pds = org.springframework.beans.BeanUtils
					.getPropertyDescriptors(clazz);
			for (PropertyDescriptor pd : pds) {
				String propertyName = pd.getName();
				if (pd.getReadMethod() == null
						|| pd.getWriteMethod() == null
						&& pd.getReadMethod().getAnnotation(UiConfig.class) == null)
					continue;
				Transient trans = pd.getReadMethod().getAnnotation(
						Transient.class);
				if (trans == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							trans = f.getAnnotation(Transient.class);
					} catch (Exception e) {
					}
				SearchableProperty searchableProperty = pd.getReadMethod()
						.getAnnotation(SearchableProperty.class);
				if (searchableProperty == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							searchableProperty = f
									.getAnnotation(SearchableProperty.class);
					} catch (Exception e) {
					}
				SearchableId searchableId = pd.getReadMethod().getAnnotation(
						SearchableId.class);
				if (searchableId == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							searchableId = f.getAnnotation(SearchableId.class);
					} catch (Exception e) {
					}
				UiConfig uiConfig = pd.getReadMethod().getAnnotation(
						UiConfig.class);
				if (uiConfig == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							uiConfig = f.getAnnotation(UiConfig.class);
					} catch (Exception e) {
					}

				if (uiConfig != null && uiConfig.hidden())
					continue;
				if ("new".equals(propertyName) || "id".equals(propertyName)
						|| "class".equals(propertyName)
						|| "fieldHandler".equals(propertyName)
						|| pd.getReadMethod() == null
						|| hides.contains(propertyName))
					continue;
				Column columnannotation = pd.getReadMethod().getAnnotation(
						Column.class);
				if (columnannotation == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							columnannotation = f.getAnnotation(Column.class);
					} catch (Exception e) {
					}
				Basic basicannotation = pd.getReadMethod().getAnnotation(
						Basic.class);
				if (basicannotation == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							basicannotation = f.getAnnotation(Basic.class);
					} catch (Exception e) {
					}
				UiConfigImpl uci = new UiConfigImpl(pd.getPropertyType(),
						uiConfig);
				if (trans != null)
					uci.setExcludedFromCriterion(true);
				uci.setExcludedFromLike(true);
				if (columnannotation != null && !columnannotation.nullable()
						|| basicannotation != null
						&& !basicannotation.optional())
					uci.setRequired(true);
				Class<?> returnType = pd.getPropertyType();
				if (returnType.isEnum()) {
					uci.setType("enum");
					try {
						returnType.getMethod("getName");
						uci.setListValue("name");
					} catch (NoSuchMethodException e) {
						uci.setListKey("top");
					}
					try {
						returnType.getMethod("getDisplayName");
						uci.setListValue("displayName");
					} catch (NoSuchMethodException e) {
						uci.setListValue(uci.getListKey());
					}
					map.put(propertyName, uci);
					continue;
				} else if (Persistable.class.isAssignableFrom(returnType)) {
					JoinColumn joincolumnannotation = pd.getReadMethod()
							.getAnnotation(JoinColumn.class);
					if (joincolumnannotation == null)
						try {
							Field f = clazz.getDeclaredField(propertyName);
							if (f != null)
								joincolumnannotation = f
										.getAnnotation(JoinColumn.class);
						} catch (Exception e) {
						}
					if (joincolumnannotation != null
							&& !joincolumnannotation.nullable())
						uci.setRequired(true);
					ManyToOne manyToOne = pd.getReadMethod().getAnnotation(
							ManyToOne.class);
					if (manyToOne == null)
						try {
							Field f = clazz.getDeclaredField(propertyName);
							if (f != null)
								manyToOne = f.getAnnotation(ManyToOne.class);
						} catch (Exception e) {
						}
					if (manyToOne != null && !manyToOne.optional())
						uci.setRequired(true);
					uci.setType("listpick");
					uci.setExcludeIfNotEdited(true);
					if (StringUtils.isBlank(uci.getPickUrl())) {
						String url = ((AutoConfigPackageProvider) packageProvider)
								.getEntityUrl(returnType);
						if (url != null) {
							StringBuilder sb = new StringBuilder(url);
							sb.append("/pick");
							Set columns = new LinkedHashSet();
							columns.addAll(AnnotationUtils
									.getAnnotatedPropertyNameAndAnnotations(
											returnType, NaturalId.class)
									.keySet());
							for (String column : "fullname,name,description,code"
									.split(","))
								try {
									if (returnType.getMethod("get"
											+ StringUtils.capitalize(column),
											new Class[0]) != null) {
										if (!columns.contains("fullname")
												&& column.equals("name")
												|| !column.equals("name"))
											columns.add(column);
									}
								} catch (NoSuchMethodException e) {
								}
							if (!columns.isEmpty()) {
								sb.append("?columns="
										+ StringUtils.join(columns, ','));
							}
							uci.setPickUrl(sb.toString());
						}
					}
					map.put(propertyName, uci);
					continue;
				}
				if (returnType == Integer.TYPE || returnType == Short.TYPE
						|| returnType == Long.TYPE || returnType == Double.TYPE
						|| returnType == Float.TYPE
						|| Number.class.isAssignableFrom(returnType)) {
					if (returnType == Integer.TYPE
							|| returnType == Integer.class
							|| returnType == Short.TYPE
							|| returnType == Short.class) {
						uci.setInputType("number");
						uci.addCssClass("integer");

					} else if (returnType == Long.TYPE
							|| returnType == Long.class) {
						uci.setInputType("number");
						uci.addCssClass("long");
					} else if (returnType == Double.TYPE
							|| returnType == Double.class
							|| returnType == Float.TYPE
							|| returnType == Float.class
							|| returnType == BigDecimal.class) {
						uci.setInputType("number");
						uci.addCssClass("double");
					}
					Set<String> cssClasses = uci.getCssClasses();
					if (cssClasses.contains("double")
							&& !uci.getDynamicAttributes().containsKey("step"))
						uci.getDynamicAttributes().put("step", "0.01");
					if (cssClasses.contains("positive")
							&& !uci.getDynamicAttributes().containsKey("min")) {
						uci.getDynamicAttributes().put("min", "1");
						if (cssClasses.contains("double"))
							uci.getDynamicAttributes().put("min", "0.01");
						if (cssClasses.contains("zero"))
							uci.getDynamicAttributes().put("min", "0");
					}
				} else if (Date.class.isAssignableFrom(returnType)) {
					uci.addCssClass("date");
					if (StringUtils.isBlank(uci.getCellEdit()))
						uci.setCellEdit("click,date");
				} else if (String.class == returnType
						&& pd.getName().toLowerCase().contains("email")
						&& !pd.getName().contains("Password")) {
					uci.setInputType("email");
					uci.addCssClass("email");
				} else if (returnType == Boolean.TYPE
						|| returnType == Boolean.class) {
					uci.setType("checkbox");
				}
				if (columnannotation != null && columnannotation.unique())
					uci.setUnique(true);
				if (searchableProperty != null || searchableId != null)
					uci.setSearchable(true);

				if (getNaturalIds().containsKey(pd.getName())) {
					uci.setRequired(true);
					if (getNaturalIds().size() == 1)
						uci.addCssClass("checkavailable");
				}
				map.put(propertyName, uci);
			}
			Map<String, UiConfigImpl> sortedMap = new TreeMap<String, UiConfigImpl>(
					new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							UiConfigImpl uci1 = map.get(o1);
							UiConfigImpl uci2 = map.get(o2);
							if (uci1 == null)
								return -1;
							if (uci2 == null)
								return 1;
							int i = Integer.valueOf(uci1.getDisplayOrder())
									.compareTo(uci2.getDisplayOrder());
							return i != 0 ? i : o1.compareTo(o2);
						}
					});
			sortedMap.putAll(map);
			uiConfigs = sortedMap;
		}
		return uiConfigs;
	}

	public Map<String, UiConfigImpl> getPropertyNamesInCriterion() {
		if (propertyNamesInCriterion == null) {
			propertyNamesInCriterion = new LinkedHashMap<String, UiConfigImpl>();
			for (Map.Entry<String, UiConfigImpl> entry : getUiConfigs()
					.entrySet()) {
				if (!entry.getKey().endsWith("AsString")
						&& !isExcludedFromCriterion(entry.getKey())
						&& !CriterionOperator.getSupportedOperators(
								entry.getValue().getPropertyType()).isEmpty())
					propertyNamesInCriterion.put(entry.getKey(),
							entry.getValue());
			}
		}
		return propertyNamesInCriterion;
	}

	protected <T extends Persistable<?>> BaseManager<T> getEntityManager(
			Class<T> entityClass) {
		return ApplicationContextUtils.getEntityManager(entityClass);
	}

	private void tryFindEntity() {
		BaseManager entityManager = getEntityManager(getEntityClass());
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass()
					.newInstance());
			bw.setConversionService(conversionService);
			Set<String> naturalIds = getNaturalIds().keySet();
			if (StringUtils.isNotBlank(getUid())) {
				bw.setPropertyValue("id", getUid());
				Serializable id = (Serializable) bw.getPropertyValue("id");
				entity = entityManager.get(id);
				if (entity == null && naturalIds.size() == 1) {
					String naturalIdName = naturalIds.iterator().next();
					bw.setPropertyValue(naturalIdName, getUid());
					id = (Serializable) bw.getPropertyValue(naturalIdName);
					entity = entityManager.findByNaturalId(id);
				}
			}
			if (entity == null && naturalIds.size() > 0) {
				Serializable[] paramters = new Serializable[naturalIds.size() * 2];
				int i = 0;
				boolean satisfied = true;
				for (String naturalId : naturalIds) {
					paramters[i] = naturalId;
					i++;
					bw.setPropertyValue(naturalId, ServletActionContext
							.getRequest().getParameter(naturalId));
					Serializable value = (Serializable) bw
							.getPropertyValue(naturalId);
					if (value != null)
						paramters[i] = value;
					else {
						satisfied = false;
						break;
					}
					i++;
				}
				if (satisfied)
					entity = entityManager.findByNaturalId(paramters);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public String execute() throws Exception {
		BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass().newInstance());
		bw.setConversionService(conversionService);
		Richtable richtableConfig = getClass().getAnnotation(Richtable.class);
		if (richtableConfig == null)
			richtableConfig = getEntityClass().getAnnotation(Richtable.class);
		final BaseManager entityManager = getEntityManager(getEntityClass());
		Map<String, String> aliases = new HashMap<String, String>();
		boolean searchable = isSearchable();
		if (searchable
				&& StringUtils.isNumeric(keyword)
				|| StringUtils.isAlphanumeric(keyword)
				&& (keyword.length() == 32 || keyword.length() >= 22
						&& keyword.length() <= 24)) // keyword is id
			try {
				bw.setPropertyValue("id", keyword);
				Serializable idvalue = (Serializable) bw.getPropertyValue("id");
				if (idvalue != null) {
					Persistable p = getEntityManager(getEntityClass()).get(
							idvalue);
					if (p != null) {
						resultPage = new ResultPage();
						resultPage.setPageNo(1);
						resultPage.setTotalResults(1);
						resultPage.setResult(Collections.singletonList(p));
						return LIST;
					}
				}
			} catch (Exception e) {

			}

		Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();
		if (ownerProperty != null
				&& ownerProperty.getKey().isolate()
				|| (!searchable || StringUtils.isBlank(keyword) || (searchable && elasticSearchService == null))) {
			DetachedCriteria dc = entityManager.detachedCriteria();
			prepare(dc);
			if (ownerProperty != null) {
				Owner owner = ownerProperty.getKey();
				if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
						.authorize(null, owner.supervisorRole(), null))
						&& owner.isolate()) {
					UserDetails ud = AuthzUtils.getUserDetails(ownerProperty
							.getValue());
					if (ud == null)
						return ACCESSDENIED;
					dc.add(Restrictions.eq(owner.propertyName(), ud));
				}
			}
			try {
				Set<String> propertyNames = getUiConfigs().keySet();
				Map<String, String[]> parameterMap = ServletActionContext
						.getRequest().getParameterMap();
				for (String parameterName : parameterMap.keySet()) {
					String propertyName;
					String[] parameterValues;
					Object value1 = null;
					Object value2 = null;
					String operatorValue;
					if (parameterName.endsWith(CRITERION_OPERATOR_SUFFIX)) {
						propertyName = parameterName.substring(0,
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
										parameterName
												+ CRITERION_OPERATOR_SUFFIX);
					}
					if (propertyName.startsWith(getEntityName() + "."))
						propertyName = propertyName.substring(propertyName
								.indexOf('.') + 1);
					if (isExcludedFromCriterion(propertyName))
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
							Class type = bw.getPropertyType(propertyName);
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
									value1 = bw2
											.getPropertyValue(subPropertyName);
								}
								if (parameterValues.length > 1) {
									bw2.setPropertyValue(subPropertyName,
											parameterValues[1]);
									value2 = bw2
											.getPropertyValue(subPropertyName);
								}
								String alias = aliases.get(propertyName);
								if (alias == null) {
									alias = CodecUtils.randomString(4);
									dc.createAlias(propertyName, alias);
									aliases.put(propertyName, alias);
								}
								Criterion criterion = operator
										.operator(
												alias + "." + subPropertyName,
												value1, value2);
								if (criterion != null)
									dc.add(criterion);
							}
						}
					} else if (propertyNames.contains(propertyName)) {
						Class type = bw.getPropertyType(propertyName);
						if (Persistable.class.isAssignableFrom(type)) {
							BaseManager em = getEntityManager(type);
							Persistable p = em.get(parameterValues[0]);
							if (p == null) {
								try {
									p = em.findOne(parameterValues[0]);
								} catch (Exception e) {

								}
							}
							if (p != null)
								dc.add(Restrictions.eq(propertyName, p));
						} else {
							if (!operator
									.isEffective(
											type,
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
							Criterion criterion = operator.operator(
									propertyName, value1, value2);
							if (criterion != null)
								dc.add(criterion);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Set<String> propertyNamesInLike = new HashSet<String>();
			for (Map.Entry<String, UiConfigImpl> entry : getUiConfigs()
					.entrySet()) {
				if (entry.getValue().isSearchable()
						&& String.class.equals(entry.getValue()
								.getPropertyType())
						&& !entry.getValue().isExcludedFromLike())
					propertyNamesInLike.add(entry.getKey());
			}
			if (searchable && StringUtils.isNotBlank(keyword)
					&& propertyNamesInLike.size() > 0)
				dc.add(CriterionUtils.like(keyword, MatchMode.ANYWHERE,
						propertyNamesInLike.toArray(new String[0])));
			if (resultPage == null)
				resultPage = new ResultPage();
			resultPage.setCriteria(dc);

			if (richtableConfig != null
					&& StringUtils.isNotBlank(richtableConfig.order())) {
				String[] ar = richtableConfig.order().split(",");
				for (String s : ar) {
					String[] arr = s.split("\\s+", 2);
					String propertyName = arr[0];
					if (propertyName.indexOf(".") > 0) {
						String p1 = propertyName.substring(0,
								propertyName.indexOf("."));
						String p2 = propertyName.substring(propertyName
								.indexOf(".") + 1);
						Class type = bw.getPropertyType(p1);
						if (Persistable.class.isAssignableFrom(type)) {
							String alias = aliases.get(p1);
							if (alias == null) {
								alias = CodecUtils.randomString(4);
								dc.createAlias(p1, alias);
								aliases.put(p1, alias);
							}
							propertyName = alias + "." + p2;
						}
					}
					if (arr.length == 2 && arr[1].equalsIgnoreCase("asc"))
						dc.addOrder(Order.asc(propertyName));
					else if (arr.length == 2 && arr[1].equalsIgnoreCase("desc"))
						dc.addOrder(Order.desc(propertyName));
					else
						dc.addOrder(Order.asc(propertyName));
				}
			} else if (Ordered.class.isAssignableFrom(getEntityClass()))
				dc.addOrder(Order.asc("displayOrder"));
			resultPage = entityManager.findByResultPage(resultPage);
		} else {
			Set<String> searchableProperties = new HashSet<String>();
			for (Map.Entry<String, UiConfigImpl> entry : getUiConfigs()
					.entrySet()) {
				if (entry.getValue().isSearchable())
					searchableProperties.add(entry.getKey());
			}
			String query = keyword.trim();
			ElasticSearchCriteria criteria = new ElasticSearchCriteria();
			criteria.setQuery(query);
			criteria.setTypes(new String[] { getEntityName() });
			if (richtableConfig != null
					&& StringUtils.isNotBlank(richtableConfig.order())) {
				String[] ar = richtableConfig.order().split(",");
				for (String s : ar) {
					String[] arr = s.split("\\s+", 2);
					String propertyName = arr[0];
					if (searchableProperties.contains(propertyName)) {
						if (arr.length == 2 && arr[1].equalsIgnoreCase("asc"))
							criteria.addSort(propertyName, false);
						else if (arr.length == 2
								&& arr[1].equalsIgnoreCase("desc"))
							criteria.addSort(propertyName, true);
						else
							criteria.addSort(propertyName, false);
					}
				}
			} else if (Ordered.class.isAssignableFrom(getEntityClass())
					&& searchableProperties.contains("displayOrder"))
				criteria.addSort("displayOrder", false);
			if (resultPage == null)
				resultPage = new ResultPage();
			resultPage.setCriteria(criteria);
			resultPage = elasticSearchService.search(resultPage,
					new Mapper<Persistable<?>>() {
						@Override
						public Persistable map(Persistable source) {
							return entityManager.get(source.getId());
						}
					});
		}
		return LIST;
	}

	protected void prepare(DetachedCriteria dc) {

	}

	protected boolean isExcludedFromCriterion(String propertyName) {
		UiConfigImpl config = getUiConfigs().get(propertyName);
		return config != null && config.isExcludedFromCriterion();
	}

	@Override
	public String input() {
		if (getReadonly().isValue()) {
			addActionError(getText("access.denied"));
			return ACCESSDENIED;
		}
		tryFindEntity();
		if (entity != null && !entity.isNew()) {
			Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();
			if (ownerProperty != null) {
				Owner owner = ownerProperty.getKey();
				if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
						.authorize(null, owner.supervisorRole(), null))
						&& (owner.isolate() || owner.readonlyForOther())) {
					UserDetails ud = AuthzUtils.getUserDetails(ownerProperty
							.getValue());
					BeanWrapperImpl bwi = new BeanWrapperImpl(entity);
					bwi.setConversionService(conversionService);
					Object value = bwi.getPropertyValue(owner.propertyName());
					if (ud == null || value == null || !ud.equals(value)) {
						addActionError(getText("access.denied"));
						return ACCESSDENIED;
					}
				}
			}
			if (checkEntityReadonly(getReadonly().getExpression(), entity)) {
				addActionError(getText("access.denied"));
				return ACCESSDENIED;
			}
		}
		if (entity == null)
			try {
				entity = getEntityClass().newInstance();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		bw.setConversionService(conversionService);
		if (entity != null && entity.isNew()) {
			Set<String> naturalIds = getNaturalIds().keySet();
			if (getUid() != null && naturalIds.size() == 1) {
				bw.setPropertyValue(naturalIds.iterator().next(), getUid());
			}
		}
		Set<String> editablePropertyNames = getUiConfigs().keySet();
		for (String parameterName : ServletActionContext.getRequest()
				.getParameterMap().keySet()) {
			String propertyName = parameterName;
			if (propertyName.startsWith(getEntityName() + "."))
				propertyName = propertyName
						.substring(propertyName.indexOf('.') + 1);
			if (propertyName.indexOf('.') > 0) {
				String subPropertyName = propertyName.substring(propertyName
						.indexOf('.') + 1);
				propertyName = propertyName.substring(0,
						propertyName.indexOf('.'));
				if (editablePropertyNames.contains(propertyName)) {
					Class type = bw.getPropertyType(propertyName);
					if (Persistable.class.isAssignableFrom(type)) {
						String parameterValue = ServletActionContext
								.getRequest().getParameter(parameterName);
						BaseManager em = getEntityManager(type);
						Persistable value = null;
						if (subPropertyName.equals("id"))
							value = em.get(parameterValue);
						else
							try {
								value = em.findOne(subPropertyName,
										parameterValue);
							} catch (Exception e) {

							}
						bw.setPropertyValue(propertyName, value);
					}
				}
			} else if (editablePropertyNames.contains(propertyName)) {
				String parameterValue = ServletActionContext.getRequest()
						.getParameter(parameterName);
				Class type = bw.getPropertyType(propertyName);
				Object value = null;
				if (Persistable.class.isAssignableFrom(type)) {
					BaseManager em = getEntityManager(type);
					value = em.get(parameterValue);
					if (value == null) {
						try {
							value = em.findOne(parameterValue);
						} catch (Exception e) {

						}
					}
					bw.setPropertyValue(propertyName, value);
				} else {
					bw.setPropertyValue(propertyName, parameterValue);
				}
			}

		}
		putEntityToValueStack(entity);
		return INPUT;
	}

	@Override
	public String save() {
		if (getReadonly().isValue()) {
			addActionError(getText("access.denied"));
			return ACCESSDENIED;
		}
		if (!makeEntityValid())
			return INPUT;
		BeanWrapperImpl bwp = new BeanWrapperImpl(entity);
		bwp.setConversionService(conversionService);
		Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();
		if (!entity.isNew()) {
			if (ownerProperty != null) {
				Owner owner = ownerProperty.getKey();
				if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
						.authorize(null, owner.supervisorRole(), null))
						&& (ownerProperty.getKey().isolate() || ownerProperty
								.getKey().readonlyForOther())) {
					UserDetails ud = AuthzUtils.getUserDetails(ownerProperty
							.getValue());
					Object value = bwp.getPropertyValue(owner.propertyName());
					if (ud == null || value == null || !ud.equals(value)) {
						addActionError(getText("access.denied"));
						return ACCESSDENIED;
					}
				}
			}
			if (checkEntityReadonly(getReadonly().getExpression(), entity)) {
				addActionError(getText("access.denied"));
				return ACCESSDENIED;
			}
		} else {
			if (ownerProperty != null) {
				UserDetails ud = AuthzUtils.getUserDetails(ownerProperty
						.getValue());
				if (ud == null) {
					addActionError(getText("access.denied"));
					return ACCESSDENIED;
				}
				bwp.setPropertyValue(ownerProperty.getKey().propertyName(), ud);
			}
		}

		for (Map.Entry<String, UiConfigImpl> entry : getUiConfigs().entrySet()) {
			String name = entry.getKey();
			UiConfigImpl uiconfig = entry.getValue();
			Object value = bwp.getPropertyValue(name);
			if (uiconfig.isRequired()
					&& (value == null || value instanceof String
							&& StringUtils.isBlank(value.toString()))) {
				addFieldError(getEntityName() + "." + name,
						getText("validation.required"));
				return INPUT;
			}
		}
		BaseManager<Persistable<?>> entityManager = getEntityManager(getEntityClass());
		entityManager.save(entity);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	public String checkavailable() {
		return makeEntityValid() ? NONE : INPUT;
	}

	private boolean makeEntityValid() {
		boolean fromList = "cell".equalsIgnoreCase(ServletActionContext
				.getRequest().getHeader("X-Edit"));
		Map<String, UiConfigImpl> uiConfigs = getUiConfigs();
		BaseManager<Persistable<?>> entityManager = getEntityManager(getEntityClass());
		entity = constructEntity();
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		bw.setConversionService(conversionService);
		for (Map.Entry<String, UiConfigImpl> entry : uiConfigs.entrySet()) {
			String regex = entry.getValue().getRegex();
			if (StringUtils.isNotBlank(regex)) {
				Object value = bw.getPropertyValue(entry.getKey());
				if (value instanceof String) {
					String str = (String) value;
					if (StringUtils.isNotBlank(str) && !str.matches(regex)) {
						addFieldError(getEntityName() + "." + entry.getKey(),
								getText("validation.invalid"));
						return false;
					}
				}
			}
		}
		Persistable persisted = null;
		Map<String, NaturalId> naturalIds = getNaturalIds();
		boolean naturalIdMutable = isNaturalIdMutable();
		boolean caseInsensitive = AnnotationUtils
				.getAnnotatedPropertyNameAndAnnotations(getEntityClass(),
						CaseInsensitive.class).size() > 0;
		if (entity.isNew()) {
			if (naturalIds.size() > 0) {
				Serializable[] args = new Serializable[naturalIds.size() * 2];
				Iterator<String> it = naturalIds.keySet().iterator();
				int i = 0;
				try {
					while (it.hasNext()) {
						String name = it.next();
						args[i] = name;
						i++;
						args[i] = (Serializable) bw.getPropertyValue(name);
						i++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				persisted = entityManager.findOne(caseInsensitive, args);
				if (persisted != null) {
					it = naturalIds.keySet().iterator();
					while (it.hasNext()) {
						addFieldError(getEntityName() + "." + it.next(),
								getText("validation.already.exists"));
					}
					return false;
				}
				for (Map.Entry<String, UiConfigImpl> entry : uiConfigs
						.entrySet())
					if (entry.getValue().isUnique()
							&& StringUtils.isNotBlank(ServletActionContext
									.getRequest().getParameter(
											getEntityName() + '.'
													+ entry.getKey()))) {
						persisted = entityManager.findOne(entry.getKey(),
								(Serializable) bw.getPropertyValue(entry
										.getKey()));
						if (persisted != null) {
							addFieldError(
									getEntityName() + "." + entry.getKey(),
									getText("validation.already.exists"));
							return false;
						}
					}
			}
			try {
				Persistable temp = entity;
				entity = getEntityClass().newInstance();
				bw = new BeanWrapperImpl(temp);
				bw.setConversionService(conversionService);
				BeanWrapperImpl bwp = new BeanWrapperImpl(entity);
				bwp.setConversionService(conversionService);
				Set<String> editedPropertyNames = new HashSet<String>();
				String propertyName = null;
				for (String parameterName : ServletActionContext.getRequest()
						.getParameterMap().keySet()) {
					if (parameterName.startsWith(getEntityName() + '.')
							|| parameterName.startsWith("__checkbox_"
									+ getEntityName() + '.')
							|| parameterName.startsWith("__datagrid_"
									+ getEntityName() + '.')) {
						propertyName = parameterName.substring(parameterName
								.indexOf('.') + 1);
						if (propertyName.indexOf('.') > 0)
							propertyName = propertyName.substring(0,
									propertyName.indexOf('.'));
						if (propertyName.indexOf('[') > 0)
							propertyName = propertyName.substring(0,
									propertyName.indexOf('['));
					}
					UiConfigImpl uiConfig = uiConfigs.get(propertyName);
					if (uiConfig == null
							|| uiConfig.getReadonly().isValue()
							|| fromList
							&& uiConfig.getHiddenInList().isValue()
							|| !fromList
							&& uiConfig.getHiddenInInput().isValue()
							|| Persistable.class.isAssignableFrom(bwp
									.getPropertyDescriptor(propertyName)
									.getPropertyType()))
						continue;
					if (StringUtils.isNotBlank(uiConfig.getReadonly()
							.getExpression())
							&& evalBoolean(uiConfig.getReadonly()
									.getExpression(), entity,
									bwp.getPropertyValue(propertyName)))
						continue;
					if (fromList) {
						if (StringUtils.isNotBlank(uiConfig.getHiddenInList()
								.getExpression())
								&& evalBoolean(uiConfig.getHiddenInList()
										.getExpression(), entity,
										bwp.getPropertyValue(propertyName)))
							continue;
					} else {
						if (StringUtils.isNotBlank(uiConfig.getHiddenInInput()
								.getExpression())
								&& evalBoolean(uiConfig.getHiddenInInput()
										.getExpression(), entity,
										bwp.getPropertyValue(propertyName)))
							continue;
					}
					editedPropertyNames.add(propertyName);
				}
				for (String name : editedPropertyNames)
					bwp.setPropertyValue(name, bw.getPropertyValue(name));
				bw = bwp;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			if (naturalIdMutable && naturalIds.size() > 0) {
				Serializable[] args = new Serializable[naturalIds.size() * 2];
				Iterator<String> it = naturalIds.keySet().iterator();
				int i = 0;
				try {
					while (it.hasNext()) {
						String name = it.next();
						args[i] = name;
						i++;
						args[i] = (Serializable) bw.getPropertyValue(name);
						i++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				persisted = entityManager.findOne(caseInsensitive, args);
				entityManager.evict(persisted);
				if (persisted != null
						&& !persisted.getId().equals(entity.getId())) {
					it = naturalIds.keySet().iterator();
					while (it.hasNext()) {
						addFieldError(getEntityName() + "." + it.next(),
								getText("validation.already.exists"));
					}
					return false;
				}

				for (Map.Entry<String, UiConfigImpl> entry : uiConfigs
						.entrySet())
					if (entry.getValue().isUnique()
							&& StringUtils.isNotBlank(ServletActionContext
									.getRequest().getParameter(
											getEntityName() + '.'
													+ entry.getKey()))) {
						persisted = entityManager.findOne(entry.getKey(),
								(Serializable) bw.getPropertyValue(entry
										.getKey()));
						entityManager.evict(persisted);
						if (persisted != null
								&& !persisted.getId().equals(entity.getId())) {
							addFieldError(
									getEntityName() + "." + entry.getKey(),
									getText("validation.already.exists"));
							return false;
						}
					}

				if (persisted != null
						&& !persisted.getId().equals(entity.getId())) {
					persisted = null;
				}
			}
			try {
				if (persisted == null)
					persisted = entityManager.get((Serializable) bw
							.getPropertyValue("id"));
				BeanWrapperImpl bwp = new BeanWrapperImpl(persisted);
				bwp.setConversionService(conversionService);
				Set<String> editedPropertyNames = new HashSet<String>();
				String propertyName = null;
				for (String parameterName : ServletActionContext.getRequest()
						.getParameterMap().keySet()) {
					if (parameterName.startsWith(getEntityName() + '.')
							|| parameterName.startsWith("__checkbox_"
									+ getEntityName() + '.')
							|| parameterName.startsWith("__datagrid_"
									+ getEntityName() + '.')) {
						propertyName = parameterName.substring(parameterName
								.indexOf('.') + 1);
						if (propertyName.indexOf('.') > 0)
							propertyName = propertyName.substring(0,
									propertyName.indexOf('.'));
						if (propertyName.indexOf('[') > 0)
							propertyName = propertyName.substring(0,
									propertyName.indexOf('['));
					}
					UiConfigImpl uiConfig = uiConfigs.get(propertyName);
					if (uiConfig == null
							|| uiConfig.getReadonly().isValue()
							|| fromList
							&& uiConfig.getHiddenInList().isValue()
							|| !fromList
							&& uiConfig.getHiddenInInput().isValue()
							|| !naturalIdMutable
							&& naturalIds.keySet().contains(propertyName)
							|| Persistable.class.isAssignableFrom(bwp
									.getPropertyDescriptor(propertyName)
									.getPropertyType()))
						continue;
					if (StringUtils.isNotBlank(uiConfig.getReadonly()
							.getExpression())
							&& evalBoolean(uiConfig.getReadonly()
									.getExpression(), persisted,
									bwp.getPropertyValue(propertyName)))
						continue;
					if (fromList) {
						if (StringUtils.isNotBlank(uiConfig.getHiddenInList()
								.getExpression())
								&& evalBoolean(uiConfig.getHiddenInList()
										.getExpression(), entity,
										bwp.getPropertyValue(propertyName)))
							continue;
					} else {
						if (StringUtils.isNotBlank(uiConfig.getHiddenInInput()
								.getExpression())
								&& evalBoolean(uiConfig.getHiddenInInput()
										.getExpression(), entity,
										bwp.getPropertyValue(propertyName)))
							continue;
					}
					editedPropertyNames.add(propertyName);
				}
				for (String name : editedPropertyNames)
					bwp.setPropertyValue(name, bw.getPropertyValue(name));
				bw = bwp;
				entity = persisted;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		try {
			for (String propertyName : getUiConfigs().keySet()) {
				UiConfigImpl uiConfig = getUiConfigs().get(propertyName);
				Class type = bw.getPropertyDescriptor(propertyName)
						.getPropertyType();
				if (uiConfig.getReadonly().isValue() || !naturalIdMutable
						&& naturalIds.keySet().contains(propertyName)
						&& !entity.isNew()
						|| !Persistable.class.isAssignableFrom(type))
					continue;
				if (!entity.isNew()
						&& StringUtils.isNotBlank(uiConfig.getReadonly()
								.getExpression())
						&& evalBoolean(uiConfig.getReadonly().getExpression(),
								entity, bw.getPropertyValue(propertyName)))
					continue;
				String parameterValue = ServletActionContext.getRequest()
						.getParameter(getEntityName() + "." + propertyName);
				if (parameterValue == null)
					parameterValue = ServletActionContext.getRequest()
							.getParameter(
									getEntityName() + "." + propertyName
											+ ".id");
				if (parameterValue == null)
					parameterValue = ServletActionContext.getRequest()
							.getParameter(propertyName + "Id");
				if (parameterValue == null) {
					continue;
				} else if (StringUtils.isBlank(parameterValue)) {
					bw.setPropertyValue(propertyName, null);
				} else {
					String listKey = uiConfig.getListKey();
					BeanWrapperImpl temp = new BeanWrapperImpl(
							type.newInstance());
					temp.setConversionService(conversionService);
					temp.setPropertyValue(listKey, parameterValue);
					BaseManager em = getEntityManager(type);
					Object obj;
					if (listKey.equals(UiConfig.DEFAULT_LIST_KEY))
						obj = em.get((Serializable) temp
								.getPropertyValue(listKey));
					else
						obj = em.findOne(listKey,
								(Serializable) temp.getPropertyValue(listKey));
					bw.setPropertyValue(propertyName, obj);
					em = getEntityManager(getEntityClass());
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	private boolean checkEntityReadonly(String expression, Persistable<?> entity) {
		if (StringUtils.isNotBlank(expression)) {
			try {
				Template template = new Template(null, "${(" + expression
						+ ")?string!}", freemarkerManager.getConfig());
				StringWriter sw = new StringWriter();
				Map<String, Object> rootMap = new HashMap<String, Object>();
				rootMap.put("entity", entity);
				template.process(rootMap, sw);
				return sw.toString().equals("true");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean evalBoolean(String expression, Persistable<?> entity,
			Object value) {
		if (StringUtils.isNotBlank(expression)) {
			try {
				Template template = new Template(null, "${(" + expression
						+ ")?string!}", freemarkerManager.getConfig());
				StringWriter sw = new StringWriter();
				Map<String, Object> rootMap = new HashMap<String, Object>();
				rootMap.put("entity", entity);
				rootMap.put("value", value);
				template.process(rootMap, sw);
				return sw.toString().equals("true");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private Tuple<Owner, Class<? extends UserDetails>> getOwnerProperty() {
		Owner owner = getEntityClass().getAnnotation(Owner.class);
		if (owner == null)
			return null;
		String propertyName = owner.propertyName();
		if (StringUtils.isBlank(propertyName))
			return null;
		BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass());
		bw.setConversionService(conversionService);
		Class type = bw.getPropertyType(propertyName);
		if (type == null)
			throw new IllegalArgumentException("No Such property "
					+ propertyName + " of bean " + getEntityClass());
		if (!UserDetails.class.isAssignableFrom(type))
			throw new IllegalArgumentException("property " + propertyName
					+ " of bean " + getEntityClass() + " is not instanceof "
					+ UserDetails.class);
		return new Tuple<Owner, Class<? extends UserDetails>>(owner, type);
	}

	@Override
	public String view() {
		tryFindEntity();
		if (entity == null)
			return NOTFOUND;
		Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();
		if (ownerProperty != null) {
			Owner owner = ownerProperty.getKey();
			if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
					.authorize(null, owner.supervisorRole(), null))
					&& owner.isolate()) {
				UserDetails ud = AuthzUtils.getUserDetails(ownerProperty
						.getValue());
				BeanWrapperImpl bwi = new BeanWrapperImpl(entity);
				bwi.setConversionService(conversionService);
				Object value = bwi.getPropertyValue(owner.propertyName());
				if (ud == null || value == null || !ud.equals(value)) {
					addActionError(getText("access.denied"));
					return ACCESSDENIED;
				}
			}
		}
		putEntityToValueStack(entity);
		return VIEW;
	}

	@Override
	public String delete() {
		if (getReadonly().isValue() && !getReadonly().isDeletable()) {
			addActionError(getText("access.denied"));
			return ACCESSDENIED;
		}

		BaseManager<Persistable<?>> entityManager = getEntityManager(getEntityClass());
		String[] arr = getId();
		Serializable[] id = (arr != null) ? new Serializable[arr.length]
				: new Serializable[0];
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass()
					.newInstance());
			bw.setConversionService(conversionService);
			for (int i = 0; i < id.length; i++) {
				bw.setPropertyValue("id", arr[i]);
				id[i] = (Serializable) bw.getPropertyValue("id");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (id.length > 0) {
			Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();

			UserDetails ud = null;
			if (ownerProperty != null) {
				Owner owner = ownerProperty.getKey();
				if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
						.authorize(null, owner.supervisorRole(), null))) {
					ud = AuthzUtils.getUserDetails(ownerProperty.getValue());
					if (ud == null) {
						addActionError(getText("access.denied"));
						return ACCESSDENIED;
					}
				}
			}
			boolean deletable = true;
			String expression = getReadonly().getExpression();
			if (ownerProperty != null || StringUtils.isNotBlank(expression)) {
				for (Serializable uid : id) {
					Persistable<?> en = entityManager.get(uid);
					if (en == null)
						continue;
					if (ownerProperty != null) {
						Owner owner = ownerProperty.getKey();
						if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
								.authorize(null, owner.supervisorRole(), null))
								&& (owner.isolate() || owner.readonlyForOther())) {
							BeanWrapperImpl bwi = new BeanWrapperImpl(en);
							bwi.setConversionService(conversionService);
							Object value = bwi.getPropertyValue(owner
									.propertyName());
							if (value == null || !ud.equals(value)) {
								addActionError(getText("delete.forbidden",
										new String[] { en.toString() }));
								deletable = false;
								break;
							}
						}
					}
					if (StringUtils.isNotBlank(expression)
							&& checkEntityReadonly(expression, en)
							&& !getReadonly().isDeletable()) {
						addActionError(getText("delete.forbidden",
								new String[] { en.toString() }));
						deletable = false;
						break;
					}
				}
			}
			if (deletable) {
				List<Persistable<?>> list = entityManager.delete(id);
				if (list.size() > 0)
					addActionMessage(getText("delete.success"));
			}
		}
		return SUCCESS;
	}

	public String enable() {
		return updateEnabled(true);
	}

	public String disable() {
		return updateEnabled(false);
	}

	private String updateEnabled(boolean enabled) {
		if (!isEnableable() || getReadonly().isValue())
			return ACCESSDENIED;
		BaseManager<Persistable<?>> em = getEntityManager(getEntityClass());
		String[] arr = getId();
		Serializable[] id = (arr != null) ? new Serializable[arr.length]
				: new Serializable[0];
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass()
					.newInstance());
			bw.setConversionService(conversionService);
			for (int i = 0; i < id.length; i++) {
				bw.setPropertyValue("id", arr[i]);
				id[i] = (Serializable) bw.getPropertyValue("id");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (id.length > 0) {
			Tuple<Owner, Class<? extends UserDetails>> ownerProperty = getOwnerProperty();
			UserDetails ud = null;
			if (ownerProperty != null) {
				Owner owner = ownerProperty.getKey();
				if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
						.authorize(null, owner.supervisorRole(), null))) {
					ud = AuthzUtils.getUserDetails(ownerProperty.getValue());
					if (ud == null) {
						addActionError(getText("access.denied"));
						return ACCESSDENIED;
					}
				}
			}
			for (Serializable s : id) {
				Enableable en = (Enableable) em.get(s);
				if (en == null || en.isEnabled() == enabled)
					continue;
				if (ownerProperty != null) {
					Owner owner = ownerProperty.getKey();
					if (!(StringUtils.isNotBlank(owner.supervisorRole()) && AuthzUtils
							.authorize(null, owner.supervisorRole(), null))
							&& (owner.isolate() || owner.readonlyForOther())) {
						BeanWrapperImpl bwi = new BeanWrapperImpl(en);
						bwi.setConversionService(conversionService);
						Object value = bwi.getPropertyValue(owner
								.propertyName());
						if (value == null || !ud.equals(value))
							continue;
					}
				}
				String expression = getReadonly().getExpression();
				if (StringUtils.isNotBlank(expression)
						&& checkEntityReadonly(expression, (Persistable<?>) en))
					continue;
				en.setEnabled(enabled);
				em.save((Persistable) en);
			}
			addActionMessage(getText("operate.success"));
		}
		return SUCCESS;
	}

	@Override
	protected Authorize findAuthorize() {
		Authorize authorize = super.findAuthorize();
		if (authorize == null) {
			Class<?> c = getEntityClass();
			return c.getAnnotation(Authorize.class);
		}
		return authorize;
	}

	public static class UiConfigImpl implements Serializable {
		private static final long serialVersionUID = -5963246979386241924L;
		private Class<?> propertyType;
		private String id;
		private String type = UiConfig.DEFAULT_TYPE;
		private String inputType = UiConfig.DEFAULT_INPUT_TYPE;
		private boolean required;
		private boolean unique;
		private int maxlength;
		private String regex;
		private Set<String> cssClasses = new LinkedHashSet<>(0);
		private ReadonlyImpl readonly;
		private int displayOrder = Integer.MAX_VALUE;
		private String alias;
		private HiddenImpl hiddenInList;
		private HiddenImpl hiddenInInput;
		private HiddenImpl hiddenInView;
		private String template;
		private String listTemplate;
		private String viewTemplate;
		private String width;
		private Map<String, String> dynamicAttributes = new HashMap<String, String>(
				0);
		private String cellDynamicAttributes = "";
		private boolean excludeIfNotEdited;
		private String listKey = UiConfig.DEFAULT_LIST_KEY;
		private String listValue = UiConfig.DEFAULT_LIST_VALUE;
		private String cellEdit = "";
		private boolean searchable;
		private String pickUrl = "";
		private String templateName = "";
		private boolean excludedFromLike = false;
		private boolean excludedFromCriterion = false;

		public UiConfigImpl() {
		}

		public UiConfigImpl(Class<?> propertyType, UiConfig config) {
			this.propertyType = propertyType;
			if (config == null)
				return;
			if (StringUtils.isNotBlank(config.id()))
				this.id = config.id();
			this.type = config.type();
			this.inputType = config.inputType();
			this.listKey = config.listKey();
			this.listValue = config.listValue();
			this.required = config.required();
			this.unique = config.unique();
			this.maxlength = config.maxlength();
			this.regex = config.regex();
			this.readonly = new ReadonlyImpl(config.readonly());
			this.displayOrder = config.displayOrder();
			if (StringUtils.isNotBlank(config.alias()))
				this.alias = config.alias();
			this.hiddenInList = new HiddenImpl(config.hiddenInList());
			this.hiddenInInput = new HiddenImpl(config.hiddenInInput());
			this.hiddenInView = new HiddenImpl(config.hiddenInView());
			this.template = config.template();
			this.listTemplate = config.listTemplate();
			this.viewTemplate = config.viewTemplate();
			this.width = config.width();
			if (StringUtils.isNotBlank(config.dynamicAttributes()))
				try {
					this.dynamicAttributes = JsonUtils.fromJson(
							config.dynamicAttributes(),
							new TypeReference<Map<String, String>>() {
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			this.cellDynamicAttributes = config.cellDynamicAttributes();
			this.cellEdit = config.cellEdit();
			this.excludeIfNotEdited = config.excludeIfNotEdited();
			if (StringUtils.isNotBlank(config.cssClass()))
				this.cssClasses.addAll(Arrays.asList(config.cssClass().split(
						"\\s")));
			this.searchable = config.searchable();
			this.pickUrl = config.pickUrl();
			this.templateName = config.templateName();
			if (StringUtils.isNotBlank(this.regex)) {
				cssClasses.add("regex");
				dynamicAttributes.put("data-regex", this.regex);
			}
			this.excludedFromLike = config.excludedFromLike();
			this.excludedFromCriterion = config.excludedFromCriterion();
		}

		public boolean isExcludedFromLike() {
			return excludedFromLike;
		}

		public void setExcludedFromLike(boolean excludedFromLike) {
			this.excludedFromLike = excludedFromLike;
		}

		public boolean isExcludedFromCriterion() {
			return excludedFromCriterion;
		}

		public void setExcludedFromCriterion(boolean excludedFromCriterion) {
			this.excludedFromCriterion = excludedFromCriterion;
		}

		public Class<?> getPropertyType() {
			return propertyType;
		}

		public void setPropertyType(Class<?> propertyType) {
			this.propertyType = propertyType;
		}

		public HiddenImpl getHiddenInList() {
			return hiddenInList;
		}

		public void setHiddenInList(HiddenImpl hiddenInList) {
			this.hiddenInList = hiddenInList;
		}

		public HiddenImpl getHiddenInInput() {
			return hiddenInInput;
		}

		public void setHiddenInInput(HiddenImpl hiddenInInput) {
			this.hiddenInInput = hiddenInInput;
		}

		public HiddenImpl getHiddenInView() {
			return hiddenInView;
		}

		public void setHiddenInView(HiddenImpl hiddenInView) {
			this.hiddenInView = hiddenInView;
		}

		public String getPickUrl() {
			return pickUrl;
		}

		public void setPickUrl(String pickUrl) {
			this.pickUrl = pickUrl;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}

		public Map<String, String> getDynamicAttributes() {
			return dynamicAttributes;
		}

		public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
			this.dynamicAttributes = dynamicAttributes;
		}

		public String getCellDynamicAttributes() {
			return cellDynamicAttributes;
		}

		public void setCellDynamicAttributes(String cellDynamicAttributes) {
			this.cellDynamicAttributes = cellDynamicAttributes;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public boolean isUnique() {
			return unique;
		}

		public void setUnique(boolean unique) {
			this.unique = unique;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public int getDisplayOrder() {
			return displayOrder;
		}

		public void setDisplayOrder(int displayOrder) {
			this.displayOrder = displayOrder;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getInputType() {
			return inputType;
		}

		public void setInputType(String inputType) {
			this.inputType = inputType;
		}

		public int getMaxlength() {
			return maxlength;
		}

		public void setMaxlength(int maxlength) {
			this.maxlength = maxlength;
		}

		public String getRegex() {
			return regex;
		}

		public void setRegex(String regex) {
			this.regex = regex;
		}

		public String getListKey() {
			return listKey;
		}

		public void setListKey(String listKey) {
			this.listKey = listKey;
		}

		public String getListValue() {
			return listValue;
		}

		public void setListValue(String listValue) {
			this.listValue = listValue;
		}

		public String getCssClass() {
			if (required)
				addCssClass("required");
			if (unique)
				addCssClass("checkavailable");
			if (excludeIfNotEdited)
				addCssClass("excludeIfNotEdited");
			return StringUtils.join(cssClasses, " ");
		}

		public void addCssClass(String cssClass) {
			this.cssClasses.add(cssClass);
		}

		public Set<String> getCssClasses() {
			return cssClasses;
		}

		public ReadonlyImpl getReadonly() {
			return readonly;
		}

		public void setReadonly(ReadonlyImpl readonly) {
			this.readonly = readonly;
		}

		public String getTemplate() {
			return template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

		public String getListTemplate() {
			return listTemplate;
		}

		public void setListTemplate(String listTemplate) {
			this.listTemplate = listTemplate;
		}

		public String getViewTemplate() {
			return viewTemplate;
		}

		public void setViewTemplate(String viewTemplate) {
			this.viewTemplate = viewTemplate;
		}

		public String getWidth() {
			return width;
		}

		public void setWidth(String width) {
			this.width = width;
		}

		public boolean isExcludeIfNotEdited() {
			return excludeIfNotEdited;
		}

		public void setExcludeIfNotEdited(boolean excludeIfNotEdited) {
			this.excludeIfNotEdited = excludeIfNotEdited;
		}

		public String getCellEdit() {
			return cellEdit;
		}

		public void setCellEdit(String cellEdit) {
			this.cellEdit = cellEdit;
		}

		public boolean isSearchable() {
			return searchable;
		}

		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}

	}

	public static class ReadonlyImpl implements Serializable {

		private static final long serialVersionUID = 6566440254646584026L;
		private boolean value = false;
		private String expression = "";
		private boolean deletable = false;

		public ReadonlyImpl() {
		}

		public ReadonlyImpl(Readonly config) {
			if (config == null)
				return;
			this.value = config.value();
			this.expression = config.expression();
			this.deletable = config.deletable();
		}

		public boolean isValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

		public boolean isDeletable() {
			return deletable;
		}

		public void setDeletable(boolean deletable) {
			this.deletable = deletable;
		}

	}

	public static class HiddenImpl implements Serializable {

		private static final long serialVersionUID = 6566440254646584026L;
		private boolean value = false;
		private String expression = "";

		public HiddenImpl() {
		}

		public HiddenImpl(Hidden config) {
			if (config == null)
				return;
			this.value = config.value();
			this.expression = config.expression();
			if (!this.value && StringUtils.isBlank(this.expression)
					&& config.hideWhenBlank())
				this.expression = "!(value?? && value?string?has_content)";
		}

		public boolean isValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

	}

	public static class RichtableImpl implements Serializable {

		private static final long serialVersionUID = 7346213812241502993L;
		private String formid = "";
		private boolean filterable = true;
		private boolean showPageSize = true;
		private boolean searchable;
		private String actionColumnButtons = "";
		private String bottomButtons = "";
		private String listHeader = "";
		private String listFooter = "";
		private String formHeader = "";
		private String formFooter = "";
		private String rowDynamicAttributes = "";

		public RichtableImpl() {
		}

		public RichtableImpl(Richtable config) {
			if (config == null)
				return;
			this.formid = config.formid();
			this.filterable = config.filterable();
			this.showPageSize = config.showPageSize();
			this.searchable = config.searchable();
			this.actionColumnButtons = config.actionColumnButtons();
			this.bottomButtons = config.bottomButtons();
			this.listHeader = config.listHeader();
			this.listFooter = config.listFooter();
			this.formHeader = config.formHeader();
			this.formFooter = config.formFooter();
			this.rowDynamicAttributes = config.rowDynamicAttributes();
		}

		public String getFormid() {
			return formid;
		}

		public void setFormid(String formid) {
			this.formid = formid;
		}

		public boolean isFilterable() {
			return filterable;
		}

		public void setFilterable(boolean filterable) {
			this.filterable = filterable;
		}

		public boolean isShowPageSize() {
			return showPageSize;
		}

		public void setShowPageSize(boolean showPageSize) {
			this.showPageSize = showPageSize;
		}

		public boolean isSearchable() {
			return searchable;
		}

		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}

		public String getActionColumnButtons() {
			return actionColumnButtons;
		}

		public void setActionColumnButtons(String actionColumnButtons) {
			this.actionColumnButtons = actionColumnButtons;
		}

		public String getBottomButtons() {
			return bottomButtons;
		}

		public void setBottomButtons(String bottomButtons) {
			this.bottomButtons = bottomButtons;
		}

		public String getListHeader() {
			return listHeader;
		}

		public void setListHeader(String listHeader) {
			this.listHeader = listHeader;
		}

		public String getListFooter() {
			return listFooter;
		}

		public void setListFooter(String listFooter) {
			this.listFooter = listFooter;
		}

		public String getFormHeader() {
			return formHeader;
		}

		public void setFormHeader(String formHeader) {
			this.formHeader = formHeader;
		}

		public String getFormFooter() {
			return formFooter;
		}

		public void setFormFooter(String formFooter) {
			this.formFooter = formFooter;
		}

		public String getRowDynamicAttributes() {
			return rowDynamicAttributes;
		}

		public void setRowDynamicAttributes(String rowDynamicAttributes) {
			this.rowDynamicAttributes = rowDynamicAttributes;
		}

	}

	// need call once before view
	protected Class<Persistable<?>> getEntityClass() {
		if (entityClass == null)
			entityClass = (Class<Persistable<?>>) ReflectionUtils
					.getGenericClass(getClass());
		if (entityClass == null) {
			ActionProxy proxy = ActionContext.getContext()
					.getActionInvocation().getProxy();
			String actionName = getEntityName();
			String namespace = proxy.getNamespace();
			entityClass = (Class<Persistable<?>>) ((AutoConfigPackageProvider) packageProvider)
					.getEntityClass(namespace, actionName);
		}
		return entityClass;
	}

	private Class<Persistable<?>> entityClass;

	private void putEntityToValueStack(Persistable entity) {
		ValueStack vs = ActionContext.getContext().getValueStack();
		if (entity != null)
			vs.set(getEntityName(), entity);
	}

	protected Persistable constructEntity() {
		Persistable entity = null;
		try {
			entity = getEntityClass().newInstance();
			ValueStack temp = valueStackFactory.createValueStack();
			temp.set(getEntityName(), entity);
			Map<String, Object> context = temp.getContext();
			Map<String, Object> parameters = ActionContext.getContext()
					.getParameters();
			try {
				ReflectionContextState.setCreatingNullObjects(context, true);
				ReflectionContextState.setDenyMethodExecution(context, true);
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {
					String name = entry.getKey();
					if (name.startsWith(getEntityName() + "."))
						temp.setParameter(name, entry.getValue());
				}
			} finally {
				ReflectionContextState.setCreatingNullObjects(context, false);
				ReflectionContextState.setDenyMethodExecution(context, false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return entity;
	}

	@Inject("ironrhino-autoconfig")
	private transient PackageProvider packageProvider;

	@Inject
	private transient ValueStackFactory valueStackFactory;

	@Inject
	private transient FreemarkerManager freemarkerManager;

}