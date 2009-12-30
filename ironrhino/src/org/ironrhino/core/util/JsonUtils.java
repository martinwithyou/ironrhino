package org.ironrhino.core.util;

import java.lang.annotation.Annotation;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicClassIntrospector;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;

public class JsonUtils {

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
						return (a.getAnnotation(NotInJson.class) != null || a
								.getAnnotation(NotInCopy.class) != null);
					}
				});
		config.setSerializationInclusion(Inclusion.NON_NULL);
		objectMapper = new ObjectMapper().setSerializationConfig(config);
	}

	public static String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T fromJson(String json, TypeReference type)
			throws Exception {
		return (T) objectMapper.readValue(json, type);
	}

	public static <T> T fromJson(String json, Class<T> cls) throws Exception {
		return (T) objectMapper.readValue(json, cls);
	}

}
