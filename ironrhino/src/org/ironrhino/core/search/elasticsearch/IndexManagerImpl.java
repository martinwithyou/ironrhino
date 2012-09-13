package org.ironrhino.core.search.elasticsearch;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.search.SearchHit;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.metadata.ClassMetadata;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.search.elasticsearch.annotations.ExcludeFromAll;
import org.ironrhino.core.search.elasticsearch.annotations.Index;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableComponent;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableId;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;
import org.ironrhino.core.search.elasticsearch.annotations.Store;
import org.ironrhino.core.util.ClassScaner;
import org.ironrhino.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;

@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class IndexManagerImpl implements IndexManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${indexName:ironrhino}")
	private String indexName;

	private Map<String, Class> typeClassMapping;

	private Map<Class, Map<String, Object>> schemaMapping;

	@Inject
	private Client client;

	@Inject
	private SessionFactory sessionFactory;

	public String getIndexName() {
		return indexName;
	}

	@PostConstruct
	public void init() {
		Set<Class<?>> set = ClassScaner.scanAnnotated(
				ClassScaner.getAppPackages(), Searchable.class);
		typeClassMapping = new HashMap<String, Class>(set.size());
		schemaMapping = new HashMap<Class, Map<String, Object>>(set.size());
		for (Class c : set) {
			Searchable searchable = (Searchable) c
					.getAnnotation(Searchable.class);
			if (!searchable.root() || c.getSimpleName().contains("$"))
				continue;
			typeClassMapping.put(classToType(c), c);
			schemaMapping.put(c, getSchemaMapping(c, false));
		}
		initialize();
	}

	private static Map<String, Object> getSchemaMapping(Class c,
			boolean component) {
		Map<String, Object> mapping = new HashMap<String, Object>();
		Map<String, Object> properties = new HashMap<String, Object>();
		if (component)
			mapping.put("type", "object");
		mapping.put("properties", properties);
		BeanWrapperImpl bw = new BeanWrapperImpl(c);
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			String name = pd.getName();
			if (name.equals("path"))
				continue;
			Method m = pd.getReadMethod();
			Class propertyType = pd.getPropertyType();
			if (propertyType == null)
				continue;
			Class componentType = propertyType;
			if (propertyType.isArray()) {
				componentType = propertyType.getComponentType();
				if (componentType.isInterface())
					continue;
			} else if (Collection.class.isAssignableFrom(propertyType)) {
				Type type = m.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					ParameterizedType ptype = (ParameterizedType) type;
					Type temp = ptype.getActualTypeArguments()[0];
					if (temp instanceof Class)
						componentType = (Class) temp;
				}
				if (componentType.isInterface())
					continue;
			}

			SearchableId searchableId = null;
			SearchableProperty searchableProperty = null;
			SearchableComponent searchableComponent = null;
			if (m != null) {
				searchableId = m.getAnnotation(SearchableId.class);
				searchableProperty = m.getAnnotation(SearchableProperty.class);
				searchableComponent = m
						.getAnnotation(SearchableComponent.class);
			}
			try {
				Field f = c.getDeclaredField(name);
				if (f != null) {
					if (searchableId == null)
						searchableId = f.getAnnotation(SearchableId.class);
					if (searchableProperty == null)
						searchableProperty = f
								.getAnnotation(SearchableProperty.class);
					if (searchableComponent == null)
						searchableComponent = f
								.getAnnotation(SearchableComponent.class);
				}
			} catch (Exception e) {
			}
			if (searchableId != null) {
				properties.put(name, new PropertyMapping(searchableId));
			} else if (searchableProperty != null) {
				properties.put(name, new PropertyMapping(componentType,
						searchableProperty));
			} else if (searchableComponent != null) {
				properties.put(name, getSchemaMapping(componentType, true));
			}
		}
		return mapping;
	}

	public static class PropertyMapping {
		private String type = "string";
		private String format;
		private Float boost;
		private String index;
		private String store;
		private String analyzer;
		private Boolean include_in_all;

		public PropertyMapping() {

		}

		public PropertyMapping(SearchableId searchableId) {
			if (StringUtils.isNotBlank(searchableId.converter()))
				this.type = searchableId.converter();
			if (StringUtils.isNotBlank(searchableId.format()))
				this.format = searchableId.format();
			if (StringUtils.isBlank(this.type))
				this.type = "string";
			type = translateType(type);
			if (searchableId.boost() != 1.0f)
				this.boost = searchableId.boost();
			Index index = searchableId.index();
			if (index == Index.NO || index == Index.ANALYZED
					|| index == Index.NOT_ANALYZED)
				this.index = index.name().toLowerCase();
			Store store = searchableId.store();
			if (store != Store.NA)
				this.store = store.name().toLowerCase();
			if (StringUtils.isNotBlank(searchableId.analyzer()))
				this.analyzer = searchableId.analyzer();
			ExcludeFromAll excludeFromAll = searchableId.excludeFromAll();
			if (excludeFromAll != ExcludeFromAll.NO)
				include_in_all = excludeFromAll == ExcludeFromAll.NO;
		}

		public PropertyMapping(Class propertyClass,
				SearchableProperty searchableProperty) {
			Class ctype = searchableProperty.type();
			if (ctype == Object.class)
				ctype = propertyClass;
			if (ctype.isPrimitive())
				this.type = ctype.toString();
			else if (ctype.isEnum())
				this.type = "string";
			else
				this.type = ctype.getSimpleName().toLowerCase();
			if (StringUtils.isNotBlank(searchableProperty.converter()))
				this.type = searchableProperty.converter();
			type = translateType(type);
			if (StringUtils.isNotBlank(searchableProperty.format()))
				this.format = searchableProperty.format();
			if (searchableProperty.boost() != 1.0f)
				this.boost = searchableProperty.boost();
			Index index = searchableProperty.index();
			if (index == Index.NO || index == Index.ANALYZED
					|| index == Index.NOT_ANALYZED)
				this.index = index.name().toLowerCase();
			Store store = searchableProperty.store();
			if (store != Store.NA)
				this.store = store.name().toLowerCase();
			if (StringUtils.isNotBlank(searchableProperty.analyzer()))
				this.analyzer = searchableProperty.analyzer();
			ExcludeFromAll excludeFromAll = searchableProperty.excludeFromAll();
			if (excludeFromAll != ExcludeFromAll.NO)
				include_in_all = excludeFromAll == ExcludeFromAll.NO;
		}

		private static String translateType(String input) {
			if (input.equals("int"))
				return "integer";
			if (input.equals("bigdecimal"))
				return "double";
			return input;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public Float getBoost() {
			return boost;
		}

		public void setBoost(Float boost) {
			this.boost = boost;
		}

		public String getIndex() {
			return index;
		}

		public void setIndex(String index) {
			this.index = index;
		}

		public String getStore() {
			return store;
		}

		public void setStore(String store) {
			this.store = store;
		}

		public String getAnalyzer() {
			return analyzer;
		}

		public void setAnalyzer(String analyzer) {
			this.analyzer = analyzer;
		}

		public Boolean getInclude_in_all() {
			return include_in_all;
		}

		public void setInclude_in_all(Boolean include_in_all) {
			this.include_in_all = include_in_all;
		}

	}

	private String entityToDocument(Persistable entity) {
		Map<String, Object> map = new HashMap<String, Object>();
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			String name = pd.getName();
			boolean searchable = false;
			Method m = pd.getReadMethod();
			if (m != null
					&& (m.getAnnotation(SearchableId.class) != null
							|| m.getAnnotation(SearchableProperty.class) != null || m
							.getAnnotation(SearchableComponent.class) != null))
				searchable = true;
			if (!searchable) {

				try {
					Field f = entity.getClass().getDeclaredField(name);
					if (f != null
							&& (f.getAnnotation(SearchableId.class) != null
									|| f.getAnnotation(SearchableProperty.class) != null || f
									.getAnnotation(SearchableComponent.class) != null))
						searchable = true;
				} catch (Exception e) {
				}
			}
			if (searchable) {
				Object value = bw.getPropertyValue(name);
				if (value != null
						&& !value.getClass().getSimpleName().contains("$"))
					map.put(name, value);
			}
		}
		return JsonUtils.toJson(map);
	}

	private static String classToType(Class clazz) {
		String type = StringUtils.uncapitalize(clazz.getSimpleName());
		Searchable s = (Searchable) clazz.getAnnotation(Searchable.class);
		if (s != null && StringUtils.isNotBlank(s.type()))
			type = s.type();
		return type;
	}

	private Class typeToClass(String type) {
		return typeClassMapping.get(type);
	}

	public Object searchHitToEntity(SearchHit sh) throws Exception {
		return JsonUtils.fromJson(sh.sourceAsString(),
				typeToClass(sh.getType()));
	}

	public ListenableActionFuture<IndexResponse> index(Persistable entity) {
		return client
				.prepareIndex(getIndexName(), classToType(entity.getClass()),
						String.valueOf(entity.getId()))
				.setSource(entityToDocument(entity)).execute();
	}

	public ListenableActionFuture<DeleteResponse> delete(Persistable entity) {
		return client.prepareDelete(getIndexName(),
				classToType(entity.getClass()), String.valueOf(entity.getId()))
				.execute();
	}

	private void initialize() {
		IndicesAdminClient adminClient = client.admin().indices();
		adminClient.create(new CreateIndexRequest(getIndexName()));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (Map.Entry<Class, Map<String, Object>> entry : schemaMapping
				.entrySet())
			try {
				HashMap<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
				map.put(classToType(entry.getKey()), entry.getValue());
				String mapping = JsonUtils.toJson(map);
				logger.info("Mapping {} : {}", entry.getKey(), mapping);
				adminClient.preparePutMapping(getIndexName())
						.setType(classToType(entry.getKey()))
						.setSource(mapping).execute().get();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
	}

	public void rebuild() {
		IndicesAdminClient adminClient = client.admin().indices();
		adminClient.delete(new DeleteIndexRequest(getIndexName()));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		initialize();
		for (Class c : schemaMapping.keySet())
			indexAll(classToType(c));
	}

	public void indexAll(String type) {
		Class clz = typeToClass(type);
		ClassMetadata md = sessionFactory.getClassMetadata(clz);
		Set<String> lazySet = new HashSet<String>();
		for (int i = 0; i < md.getPropertyTypes().length; i++) {
			org.hibernate.type.Type t = md.getPropertyTypes()[i];
			if (t.isEntityType()) {
				lazySet.add(t.getName());
			} else if (t.isCollectionType()) {
				lazySet.add(t.getName());
			}
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(clz);
		c.setProjection(Projections.projectionList()
				.add(Projections.rowCount()));
		long count = (Long) c.uniqueResult();
		c = session.createCriteria(clz);
		for (String s : lazySet)
			c.setFetchMode(s, FetchMode.JOIN);
		int maxResults = 50;
		int firstResult = 0;
		while (firstResult < count) {
			c.setFirstResult(firstResult);
			if (maxResults > 0)
				c.setMaxResults(maxResults);
			List<Persistable> list = c.list();
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			for (Persistable p : list) {
				Hibernate.initialize(p);
				bulkRequest.add(client.prepareIndex(getIndexName(),
						classToType(p.getClass()), String.valueOf(p.getId()))
						.setSource(entityToDocument(p)));
			}
			bulkRequest.execute();
			firstResult += maxResults;
		}
	}

}
