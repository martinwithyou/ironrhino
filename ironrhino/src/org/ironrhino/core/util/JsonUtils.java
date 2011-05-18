package org.ironrhino.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.metadata.NotInJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {

	private static Logger log = LoggerFactory.getLogger(JsonUtils.class);

	private static ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		SerializationConfig config = objectMapper.getSerializationConfig();
		config.setSerializationInclusion(Inclusion.NON_NULL);
		config.set(Feature.WRITE_ENUMS_USING_TO_STRING, true);
		config = config
				.withAnnotationIntrospector(new JacksonAnnotationIntrospector() {
					
					@Override
					public String findEnumValue(Enum<?> value) {
						return value.toString();
					}

					@Override
					public boolean isHandled(Annotation ann) {
						return super.isHandled(ann) || ann instanceof NotInJson;
					}

					@Override
					public boolean isIgnorableField(AnnotatedField f) {
						return super.isIgnorableField(f) || isIgnorable(f);
					}

					@Override
					public boolean isIgnorableMethod(AnnotatedMethod m) {
						return super.isIgnorableMethod(m) || isIgnorable(m);
					}

					private boolean isIgnorable(Annotated a) {
						if ((a.getAnnotation(NotInJson.class) != null))
							return true;
						if (a.getAnnotated() instanceof Method) {
							Method m = (Method) a.getAnnotated();
							Class clazz = m.getDeclaringClass();
							if (m.getParameterTypes().length > 0
									|| clazz.getName().startsWith("java."))
								return true;
							String name = a.getName();
							if (name.startsWith("get"))
								name = name.substring(3);
							else if (name.startsWith("is"))
								name = name.substring(2);
							else
								return true;
							name = StringUtils.uncapitalize(name);
							try {
								Field field = clazz.getDeclaredField(name);
								return (field.getAnnotation(NotInJson.class) != null || m
										.getAnnotation(NotInJson.class) != null);
							} catch (NoSuchFieldException e) {
								return (m.getAnnotation(NotInJson.class) != null);
							}
						}
						return false;
					}
				});
		objectMapper.setSerializationConfig(config);
		objectMapper
				.getDeserializationConfig()
				.set(org.codehaus.jackson.map.DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING,
						true);
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public static String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static <T> T fromJson(String json, TypeReference<T> type)
			throws Exception {
		return (T) objectMapper.readValue(json, type);
	}

	public static <T> T fromJson(String json, Class<T> cls) throws Exception {
		return objectMapper.readValue(json, cls);
	}

}
