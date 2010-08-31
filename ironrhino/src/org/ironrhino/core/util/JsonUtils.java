package org.ironrhino.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicClassIntrospector;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.metadata.NotInJson;

public class JsonUtils {

	private static Log log = LogFactory.getLog(JsonUtils.class);

	private static ObjectMapper objectMapper;
	static {
		SerializationConfig config = new SerializationConfig(
				new BasicClassIntrospector(),
				new JacksonAnnotationIntrospector() {

					@Override
					public String findEnumValue(Enum<?> value) {
						return value.toString();
					}

					@Override
					public boolean isHandled(Annotation ann) {
						return true;
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
						boolean b = (a.getAnnotation(NotInJson.class) != null);
						if (b)
							return b;
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
								return (field.getAnnotation(NotInJson.class) != null);
							} catch (NoSuchFieldException e) {
							}
						}
						return false;
					}
				}, null);
		config.setSerializationInclusion(Inclusion.NON_NULL);
		config.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		objectMapper = new ObjectMapper().setSerializationConfig(config);
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
