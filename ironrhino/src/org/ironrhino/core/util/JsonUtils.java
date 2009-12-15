package org.ironrhino.core.util;

import java.lang.annotation.Annotation;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicClassIntrospector;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.metadata.JsonSerializerType;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

	private static ObjectMapper objectMapper = new ObjectMapper()
			.setSerializationConfig(new SerializationConfig(
					new BasicClassIntrospector(),
					new JacksonAnnotationIntrospector() {
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
					}));

	private static Gson gson = new GsonBuilder().setDateFormat(
			DateUtils.DATETIME).create();

	public static String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			return null;
		}
	}

	public static String toJson(Object object, JsonSerializerType type) {
		if (type == JsonSerializerType.GSON)
			return gson.toJson(object);
		return toJson(object);
	}

	public static <T> T fromJson(String json, TypeReference type)
			throws Exception {
		return objectMapper.readValue(json, type);
	}

}
