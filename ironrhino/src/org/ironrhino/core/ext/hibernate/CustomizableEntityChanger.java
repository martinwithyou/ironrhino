package org.ironrhino.core.ext.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.Type;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.common.util.XmlUtils;
import org.ironrhino.core.model.Customizable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CustomizableEntityChanger {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	private ApplicationContext ctx;

	private LocalSessionFactoryBean lsfb;

	private SessionFactory sf;

	private Map<String, List<PropertyChange>> changes = new HashMap<String, List<PropertyChange>>();

	public static Map<String, Map<String, PropertyType>> customizableEntities;

	public Map<String, Map<String, PropertyType>> getCustomizableEntities() {
		return customizableEntities;
	}

	public static Map<String, PropertyType> getCustomizedProperties(
			String entityClassName) {
		Map<String, PropertyType> map = customizableEntities
				.get(entityClassName);
		return map;
	}

	public static void convertCustomPropertiesType(Customizable entity) {
		if (customizableEntities != null) {
			Map<String, PropertyType> map = customizableEntities.get(entity
					.getClass().getName());
			Map<String, Serializable> customProperties = entity
					.getCustomProperties();
			for (Map.Entry<String, Serializable> entry : customProperties
					.entrySet()) {
				String name = entry.getKey();
				Object o = entry.getValue();
				if (o == null || !(o instanceof String[]))
					continue;
				String array[] = (String[]) o;
				if (array.length == 0 || StringUtils.isBlank(array[0])) {
					customProperties.put(name, null);
					continue;
				}
				String value = array[0].trim();
				try {
					if (PropertyType.DATE == map.get(name))
						customProperties
								.put(name, DateUtils.parseDate10(value));
					else if (PropertyType.BIGDECIMAL == map.get(name))
						customProperties.put(name, new BigDecimal(value));
					else if (PropertyType.DOUBLE == map.get(name))
						customProperties.put(name, new Double(value));
					else if (PropertyType.FLOAT == map.get(name))
						customProperties.put(name, new Float(value));
					else if (PropertyType.LONG == map.get(name))
						customProperties.put(name, new Long(value));
					else if (PropertyType.INTEGER == map.get(name))
						customProperties.put(name, new Integer(value));
					else if (PropertyType.SHORT == map.get(name))
						customProperties.put(name, new Short(value));
					else if (PropertyType.BOOLEAN == map.get(name))
						customProperties.put(name, Boolean.valueOf(value));
					else
						customProperties.put(name, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		initCustomizableEntities();
	}

	private void initCustomizableEntities() {
		lsfb = (LocalSessionFactoryBean) ctx.getBean("&sessionFactory");
		sf = (SessionFactory) lsfb.getObject();
		customizableEntities = new HashMap<String, Map<String, PropertyType>>();
		Iterator it = lsfb.getConfiguration().getClassMappings();
		while (it.hasNext()) {
			PersistentClass pc = (PersistentClass) it.next();
			try {
				Class clazz = Class.forName(pc.getClassName());
				if (Customizable.class.isAssignableFrom(clazz)) {
					Map<String, PropertyType> map = new HashMap<String, PropertyType>();
					Iterator propertyIterator = getDynamicComponent(clazz)
							.getPropertyIterator();
					while (propertyIterator.hasNext()) {
						Property p = (Property) propertyIterator.next();
						Class t = p.getType().getReturnedClass();
						if (Date.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.DATE);
						else if (BigDecimal.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.BIGDECIMAL);
						else if (Double.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.DOUBLE);
						else if (Float.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.FLOAT);
						else if (Long.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.LONG);
						else if (Integer.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.INTEGER);
						else if (Short.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.SHORT);
						else if (Boolean.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.BOOLEAN);
						else
							map.put(p.getName(), PropertyType.STRING);
					}
					customizableEntities.put(clazz.getName(), map);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, List<PropertyChange>> getChanges() {
		return changes;
	}

	public void prepareChange(String entityClass, PropertyChange change)
			throws ClassNotFoundException {
		Class clazz = Class.forName(entityClass);
		if (!Customizable.class.isAssignableFrom(clazz))
			throw new IllegalArgumentException("Class '" + entityClass
					+ "' must implements interface '"
					+ Customizable.class.getName() + "'");

		List<PropertyChange> list = changes.get(entityClass);
		if (list == null) {
			list = new ArrayList<PropertyChange>();
			changes.put(entityClass, list);
		}

		Map<String, PropertyType> map = customizableEntities.get(clazz
				.getName());
		if (!change.isRemove()) {
			PropertyChange pc = null;
			for (PropertyChange temp : list)
				if (temp.getName().equals(change.getName())) {
					pc = temp;
					break;
				}
			if (pc != null) {
				if (pc.isRemove())
					list.remove(pc);
				return;
			}
			if (map.containsKey(change.getName()))
				throw new IllegalArgumentException("customized property '"
						+ change.getName() + "' already exists");
			list.add(change);
		} else {
			PropertyChange pc = null;
			for (PropertyChange temp : list)
				if (temp.getName().equals(change.getName())) {
					pc = temp;
					break;
				}
			if (pc != null) {
				if (!pc.isRemove()) {
					list.remove(pc);
					if (list.size() == 0)
						changes.remove(entityClass);
				}
				return;
			}
			if (!map.containsKey(change.getName()))
				throw new IllegalArgumentException("Customized property '"
						+ change.getName() + "' doesn't exists");
			list.add(change);
		}

	}

	public void discardChanges() {
		changes.clear();
	}

	public synchronized void applyChanges() throws Exception {
		if (changes.isEmpty())
			return;
		updatePersistentClass();
		updateMappingFile();
		updateSessionFactory();
		lsfb.updateDatabaseSchema();
		changes.clear();
		initCustomizableEntities();
	}

	private Component getDynamicComponent(Class entityClass) {
		PersistentClass pclass = getPersistentClass(entityClass);
		Iterator<Property> it = pclass.getPropertyIterator();
		boolean exists = false;
		while (it.hasNext()) {
			Property p = it.next();
			if (p.getName().equals(Customizable.CUSTOM_COMPONENT_NAME)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			Component comp = new Component(pclass);
			comp.setDynamic(true);
			Property p = new Property();
			p.setName(Customizable.CUSTOM_COMPONENT_NAME);
			p.setPersistentClass(pclass);
			p.setValue(comp);
			pclass.addProperty(p);
			return comp;
		}
		Property property = getPersistentClass(entityClass).getProperty(
				Customizable.CUSTOM_COMPONENT_NAME);
		return (Component) property.getValue();
	}

	private PersistentClass getPersistentClass(Class entityClass) {
		return lsfb.getConfiguration().getClassMapping(entityClass.getName());
	}

	private void updateSessionFactory() {
		Configuration conf = lsfb.getConfiguration();
		for (String entityClass : changes.keySet()) {
			((SessionFactoryImpl) sf).updatePersistentClass(conf
					.getClassMapping(entityClass), conf.getMapping());
		}
	}

	private void updatePersistentClass() throws Exception {
		for (String entityClass : changes.keySet()) {
			Class clazz = Class.forName(entityClass);
			for (PropertyChange change : changes.get(entityClass)) {
				if (!change.isRemove()) {
					SimpleValue simpleValue = new SimpleValue();
					if (change.getType() == PropertyType.STRING)
						simpleValue.setTypeName(String.class.getName());
					else if (change.getType() == PropertyType.SHORT)
						simpleValue.setTypeName(Short.class.getName());
					else if (change.getType() == PropertyType.INTEGER)
						simpleValue.setTypeName(Integer.class.getName());
					else if (change.getType() == PropertyType.LONG)
						simpleValue.setTypeName(Long.class.getName());
					else if (change.getType() == PropertyType.FLOAT)
						simpleValue.setTypeName(Float.class.getName());
					else if (change.getType() == PropertyType.DOUBLE)
						simpleValue.setTypeName(Double.class.getName());
					else if (change.getType() == PropertyType.BIGDECIMAL)
						simpleValue.setTypeName(BigDecimal.class.getName());
					else if (change.getType() == PropertyType.BOOLEAN)
						simpleValue.setTypeName(Boolean.class.getName());
					else if (change.getType() == PropertyType.DATE)
						simpleValue.setTypeName(Date.class.getName());
					PersistentClass persistentClass = getPersistentClass(clazz);
					simpleValue.setTable(persistentClass.getTable());
					Column c = new Column();
					c.setName(Customizable.CUSTOM_COMPONENT_NAME + "_"
							+ change.getName());
					simpleValue.addColumn(c);
					Property property = new Property();
					property.setName(change.getName());
					property.setPersistentClass(persistentClass);
					property.setValue(simpleValue);
					getDynamicComponent(clazz).addProperty(property);
				} else {
					Iterator propertyIterator = getDynamicComponent(clazz)
							.getPropertyIterator();

					while (propertyIterator.hasNext()) {
						Property property = (Property) propertyIterator.next();
						if (property.getName().equals(change.getName()))
							propertyIterator.remove();
					}
				}
			}
		}

	}

	private void updateMappingFile() throws Exception {
		for (String entityClass : changes.keySet()) {
			Class clazz = Class.forName(entityClass);
			String file = ctx.getResource(getMappingResource(clazz)).getFile()
					.getAbsolutePath();
			Node node = null;
			Document document = XmlUtils.loadDocument(file);
			NodeList componentTags = document
					.getElementsByTagName("dynamic-component");
			if (componentTags.getLength() == 0) {
				Element element = document.createElement("dynamic-component");
				element
						.setAttribute("name",
								Customizable.CUSTOM_COMPONENT_NAME);
				element.setAttribute("insert", "true");
				element.setAttribute("update", "true");
				element.setAttribute("optimistic-lock", "true");
				document.getElementsByTagName("class").item(0).appendChild(
						element);
				node = element;
			} else {
				node = componentTags.item(0);
				XmlUtils.removeChildren(node);
			}
			Iterator propertyIterator = getDynamicComponent(clazz)
					.getPropertyIterator();
			while (propertyIterator.hasNext()) {
				Property property = (Property) propertyIterator.next();
				Element element = createPropertyElement(document, property);
				node.appendChild(element);
			}
			XmlUtils.saveDocument(document, file);
			lsfb.getConfiguration().updateMapping(entityClass,
					ctx.getResource(getMappingResource(clazz)).getURL());
		}
	}

	private Element createPropertyElement(Document document, Property property) {
		Element element = document.createElement("property");
		Type type = property.getType();
		element.setAttribute("name", property.getName());
		String name = ((Column) property.getColumnIterator().next()).getName();
		if (!name.startsWith(Customizable.CUSTOM_COMPONENT_NAME + "_"))
			name = Customizable.CUSTOM_COMPONENT_NAME + "_" + name;
		element.setAttribute("column", name);
		element.setAttribute("type", type.getReturnedClass().getName());
		element.setAttribute("not-null", "false");
		return element;
	}

	protected String getMappingResource(Class entityClass) {
		String moduleName = entityClass.getName();
		moduleName = moduleName.substring(0, moduleName.indexOf(".model"));
		moduleName = moduleName.substring(moduleName.lastIndexOf('.') + 1);
		return "classpath:/resources/hibernate/" + moduleName + "/"
				+ entityClass.getSimpleName() + ".hbm.xml";
	}

}
