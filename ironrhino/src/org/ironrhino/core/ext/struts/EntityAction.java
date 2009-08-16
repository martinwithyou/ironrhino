package org.ironrhino.core.ext.struts;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.util.AnnotationUtils;
import org.ironrhino.core.ext.hibernate.CustomizableEntityChanger;
import org.ironrhino.core.ext.hibernate.PropertyType;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.FormElement;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInUI;
import org.ironrhino.core.model.Customizable;
import org.ironrhino.core.model.Entity;
import org.ironrhino.core.service.BaseManager;
import org.springframework.beans.BeanWrapperImpl;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;

public class EntityAction extends BaseAction {

	protected static Log log = LogFactory.getLog(EntityAction.class);

	private BaseManager baseManager;

	private ResultPage resultPage;

	private Entity entity;

	private boolean readonly;

	public boolean isReadonly() {
		return readonly;
	}

	public ResultPage getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage resultPage) {
		this.resultPage = resultPage;
	}

	public void setBaseManager(BaseManager baseManager) {
		this.baseManager = baseManager;
	}

	private boolean readonly() {
		AutoConfig ac = (AutoConfig) getEntityClass().getAnnotation(
				AutoConfig.class);
		return (ac != null) && ac.readonly();
	}

	public String list() {
		baseManager.setEntityClass(getEntityClass());
		DetachedCriteria dc = baseManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage();
		resultPage.setDetachedCriteria(dc);
		resultPage = baseManager.getResultPage(resultPage);
		readonly = readonly();
		return LIST;
	}

	public String input() {
		if (readonly())
			return NONE;
		baseManager.setEntityClass(getEntityClass());
		if (getUid() != null)
			entity = baseManager.get(getUid());
		if (entity == null)
			try {
				// for fetch default value by construct
				entity = (Entity) getEntityClass().newInstance();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		setEntity(entity);
		return INPUT;
	}

	public String save() {
		if (readonly())
			return NONE;
		baseManager.setEntityClass(getEntityClass());
		entity = getEntity();
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		Entity persisted = null;
		Map<String, Annotation> naturalIds = getNaturalIds();
		boolean naturalIdImmutable = isNaturalIdsImmatuable();
		boolean caseInsensitive = naturalIds.size() > 0
				&& ((NaturalId) naturalIds.values().iterator().next())
						.caseInsensitive();
		if (entity.isNew()) {
			if (naturalIds.size() > 0) {
				Object[] args = new Object[naturalIds.size() * 2];
				Iterator<String> it = naturalIds.keySet().iterator();
				int i = 0;
				try {
					while (it.hasNext()) {
						String name = it.next();
						args[i] = name;
						i++;
						args[i] = bw.getPropertyValue(name);
						i++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				persisted = baseManager.getByNaturalId(caseInsensitive, args);
				if (persisted != null) {
					it = naturalIds.keySet().iterator();
					while (it.hasNext()) {
						addFieldError(getEntityName() + "." + it.next(),
								getText("naturalId.already.exists"));
					}
					return INPUT;
				}
			}
		} else {
			if (!naturalIdImmutable && naturalIds.size() > 0) {
				Object[] args = new Object[naturalIds.size() * 2];
				Iterator<String> it = naturalIds.keySet().iterator();
				int i = 0;
				try {
					while (it.hasNext()) {
						String name = it.next();
						args[i] = name;
						i++;
						args[i] = bw.getPropertyValue(name);
						i++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				persisted = baseManager.getByNaturalId(caseInsensitive, args);
				if (persisted != null && !persisted.equals(entity)) {
					it = naturalIds.keySet().iterator();
					while (it.hasNext()) {
						addFieldError(getEntityName() + "." + it.next(),
								getText("naturalId.already.exists"));
					}
					return INPUT;
				}
				if (persisted != null && !persisted.equals(entity)) {
					baseManager.evict(persisted);
					persisted = null;
				}
			}
			try {
				if (persisted == null)
					persisted = baseManager.get((Serializable) bw
							.getPropertyValue("id"));
				BeanWrapperImpl bwp = new BeanWrapperImpl(persisted);
				Set<String> names = getFormElements().keySet();
				if (naturalIdImmutable)
					names.removeAll(naturalIds.keySet());
				for (String name : names) {
					if (!name.startsWith(Customizable.CUSTOM_COMPONENT_NAME))
						bwp.setPropertyValue(name, bw.getPropertyValue(name));
				}
				if (persisted instanceof Customizable)
					bwp
							.setPropertyValue(
									Customizable.CUSTOM_COMPONENT_NAME,
									bw
											.getPropertyValue(Customizable.CUSTOM_COMPONENT_NAME));
				entity = persisted;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		baseManager.save(entity);
		addActionMessage(getText("save.successfully"));
		return SUCCESS;
	}

	public String view() {
		baseManager.setEntityClass(getEntityClass());
		if (getUid() != null)
			entity = baseManager.get(getUid());
		setEntity(entity);
		return VIEW;
	}

	public String delete() {
		if (readonly())
			return NONE;
		baseManager.setEntityClass(getEntityClass());
		String[] arr = getId();
		Serializable[] id = (arr != null) ? new Serializable[arr.length]
				: new Serializable[0];
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass()
					.newInstance());
			for (int i = 0; i < id.length; i++) {
				bw.setPropertyValue("id", arr[i]);
				id[i] = (Serializable) bw.getPropertyValue("id");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (id.length > 0) {
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List list = baseManager.getListByCriteria(dc);
			if (list.size() > 0) {
				for (Object obj : list)
					baseManager.delete((Entity) obj);
				addActionMessage(getText("delete.success",
						"delete  successfully", new String[] { "" }));
			}
		}
		return SUCCESS;
	}

	public boolean isNew() {
		return entity == null || entity.isNew();
	}

	private Map<String, Annotation> _naturalIds;

	// need call once before view
	public String getEntityName() {
		if (entityName == null)
			entityName = ActionContext.getContext().getActionInvocation()
					.getProxy().getActionName();
		return entityName;
	}

	private String entityName;

	public Map<String, Annotation> getNaturalIds() {
		if (_naturalIds != null)
			return _naturalIds;
		_naturalIds = AnnotationUtils.getAnnotatedPropertyNameAndAnnotations(
				getEntityClass(), NaturalId.class);
		return _naturalIds;
	}

	public boolean isNaturalIdsImmatuable() {
		return getNaturalIds().size() > 0
				&& ((NaturalId) getNaturalIds().values().iterator().next())
						.immutable();
	}

	public Map<String, FormElementConfig> getFormElements() {
		Class clazz = getEntityClass();
		String key = clazz.getName();
		boolean customizable = Customizable.class.isAssignableFrom(clazz);
		if (cache.get(key) == null) {
			Set<String> notInUIs = AnnotationUtils.getAnnotatedPropertyNames(
					clazz, NotInUI.class);
			notInUIs.addAll(AnnotationUtils.getAnnotatedPropertyNames(clazz,
					NotInCopy.class));
			Map<String, FormElementConfig> map = new HashMap<String, FormElementConfig>();
			PropertyDescriptor[] pds = org.springframework.beans.BeanUtils
					.getPropertyDescriptors(clazz);

			for (PropertyDescriptor pd : pds) {
				if ("new".equals(pd.getName()) || "id".equals(pd.getName())
						|| "class".equals(pd.getName())
						|| pd.getReadMethod() == null
						|| notInUIs.contains(pd.getName()))
					continue;
				Class returnType = pd.getReadMethod().getReturnType();
				if (returnType.isEnum()) {
					FormElementConfig fes = new FormElementConfig();
					try {
						Method method = pd.getReadMethod().getReturnType()
								.getMethod("values", new Class[0]);
						fes.setEnumValues((Enum[]) method.invoke(null));
						fes.setEnumClass(returnType.getName());
						fes.setType("select");
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					map.put(pd.getName(), fes);
					continue;
				}
				FormElement fe = pd.getReadMethod().getAnnotation(
						FormElement.class);
				if (fe == null)
					try {
						Field f = clazz.getDeclaredField(pd.getName());
						if (f != null)
							fe = f.getAnnotation(FormElement.class);
					} catch (Exception e) {
					}
				FormElementConfig fec = new FormElementConfig(fe);
				if (returnType == Integer.TYPE || returnType == Integer.class
						|| returnType == Short.TYPE
						|| returnType == Short.class || returnType == Long.TYPE
						|| returnType == Long.class) {
					fec.addCssClass("integer");
				} else if (returnType == Double.TYPE
						|| returnType == Double.class
						|| returnType == Float.TYPE
						|| returnType == Float.class
						|| returnType == BigDecimal.class) {
					fec.addCssClass("double");
				} else if (Date.class.isAssignableFrom(returnType)) {
					fec.addCssClass("date");
				} else if (String.class == returnType
						&& pd.getName().toLowerCase().contains("email")) {
					fec.addCssClass("email");
				} else if (returnType == Boolean.TYPE
						|| returnType == Boolean.class)
					fec.setType("checkbox");
				if (getNaturalIds().containsKey(pd.getName()))
					fec.setRequired(true);
				map.put(pd.getName(), fec);
			}
			List<Map.Entry<String, FormElementConfig>> list = new ArrayList<Map.Entry<String, FormElementConfig>>();
			list.addAll(map.entrySet());
			Collections.sort(list,
					new Comparator<Map.Entry<String, FormElementConfig>>() {
						public int compare(Entry<String, FormElementConfig> o1,
								Entry<String, FormElementConfig> o2) {
							int i = Integer.valueOf(
									o1.getValue().getDisplayOrder()).compareTo(
									o2.getValue().getDisplayOrder());
							if (i == 0)
								return o1.getKey().compareTo(o2.getKey());
							else
								return i;
						}
					});
			map = new LinkedHashMap<String, FormElementConfig>();
			for (Map.Entry<String, FormElementConfig> entry : list)
				map.put(entry.getKey(), entry.getValue());

			if (customizable) {
				map.remove(Customizable.CUSTOM_COMPONENT_NAME);
				Map<String, PropertyType> customProperties = CustomizableEntityChanger.customizableEntities
						.get(clazz.getName());
				;
				if (customProperties != null && customProperties.size() > 0) {
					for (Map.Entry<String, PropertyType> entry : customProperties
							.entrySet()) {
						String name = entry.getKey();
						PropertyType pt = entry.getValue();
						FormElementConfig fec = new FormElementConfig();
						if (pt == PropertyType.SHORT
								|| pt == PropertyType.INTEGER
								|| pt == PropertyType.LONG) {
							fec.addCssClass("integer");
						} else if (pt == PropertyType.FLOAT
								|| pt == PropertyType.DOUBLE
								|| pt == PropertyType.BIGDECIMAL) {
							fec.addCssClass("double");
						} else if (pt == PropertyType.DATE) {
							fec.addCssClass("date");
						} else if (pt == PropertyType.STRING
								&& name.toLowerCase().contains("email")) {
							fec.addCssClass("email");
						} else if (pt == PropertyType.BOOLEAN)
							fec.setType("checkbox");
						map
								.put(Customizable.CUSTOM_COMPONENT_NAME + "."
										+ name, fec);
					}
				}
				return map;
			}
			cache.put(key, map);
		}
		return cache.get(key);
	}

	private static Map<String, Map<String, FormElementConfig>> cache = new ConcurrentHashMap<String, Map<String, FormElementConfig>>();

	public static class FormElementConfig {
		private String type = FormElement.DEFAULT_TYPE;
		private int size;
		private String enumClass;
		private Enum[] enumValues;
		private String cssClass = "";
		private boolean readonly;
		private int displayOrder;

		public FormElementConfig() {
		}

		public FormElementConfig(FormElement fe) {
			if (fe == null)
				return;
			this.type = fe.type();
			this.size = fe.size();
			this.readonly = fe.readonly();
			if (fe.required())
				this.setRequired(true);
			this.displayOrder = fe.displayOrder();
		}

		public int getDisplayOrder() {
			return displayOrder;
		}

		public void setDisplayOrder(int displayOrder) {
			this.displayOrder = displayOrder;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getSize() {
			return size;
		}

		public String getEnumClass() {
			return enumClass;
		}

		public void setEnumClass(String enumClass) {
			this.enumClass = enumClass;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public Enum[] getEnumValues() {
			return enumValues;
		}

		public void setEnumValues(Enum[] enumValues) {
			this.enumValues = enumValues;
		}

		public String getCssClass() {
			return cssClass;
		}

		public void addCssClass(String cssClass) {
			this.cssClass += " " + cssClass;
		}

		public void setRequired(boolean required) {
			if (required)
				this.cssClass += " required";
		}

		public boolean isReadonly() {
			return readonly;
		}

		public void setReadonly(boolean readonly) {
			this.readonly = readonly;
		}
	}

	// need call once before view
	private Class getEntityClass() {
		if (entityClass == null) {
			ActionProxy proxy = ActionContext.getContext()
					.getActionInvocation().getProxy();
			String actionName = getEntityName();
			String namespace = proxy.getNamespace();
			entityClass = ((AutoConfigPackageProvider) packageProvider)
					.getEntityClass(namespace, actionName);
		}
		return entityClass;
	}

	private Class entityClass;

	private void setEntity(Entity entity) {
		ValueStack vs = ActionContext.getContext().getValueStack();
		vs.set(getEntityName(), entity);
	}

	private Entity getEntity() {
		Entity entity = null;
		try {
			entity = (Entity) getEntityClass().newInstance();
			Map<String, String[]> map = ServletActionContext.getRequest()
					.getParameterMap();
			ValueStack temp = valueStackFactory.createValueStack();
			temp.set(getEntityName(), entity);
			for (Map.Entry<String, String[]> entry : map.entrySet())
				temp.setValue(entry.getKey(), entry.getValue());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return entity;
	}

	@Inject
	public void setValueStackFactory(ValueStackFactory valueStackFactory) {
		this.valueStackFactory = valueStackFactory;
	}

	private ValueStackFactory valueStackFactory;

	@Inject("ironrhino-autoconfig")
	public void setPackageProvider(PackageProvider packageProvider) {
		this.packageProvider = packageProvider;
	}

	private PackageProvider packageProvider;

}
