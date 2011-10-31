package org.ironrhino.core.util;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.Annotated;
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
		config = config
				.withSerializationInclusion(Inclusion.NON_NULL)
				.with(Feature.WRITE_ENUMS_USING_TO_STRING)
				.withAnnotationIntrospector(
						new JacksonAnnotationIntrospector() {

							@Override
							public String findEnumValue(Enum<?> value) {
								return value.toString();
							}

							@Override
							public boolean isHandled(Annotation ann) {
								return super.isHandled(ann)
										|| ann instanceof NotInJson;
							}

							protected boolean _isIgnorable(Annotated a) {
								boolean b = super._isIgnorable(a);
								if (b)
									return b;
								NotInJson ann = a
										.getAnnotation(NotInJson.class);
								return ann != null;
							}

						});
		objectMapper.setSerializationConfig(config);
		DeserializationConfig dconfig = objectMapper.getDeserializationConfig();
		dconfig = dconfig
				.with(org.codehaus.jackson.map.DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING);
		objectMapper.setDeserializationConfig(dconfig);
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

	public static boolean isValidJson(String content) {
		try {
			JsonUtils.getObjectMapper().readValue(content, JsonNode.class);
			return true;
		} catch (Exception e) {
			return false;
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
