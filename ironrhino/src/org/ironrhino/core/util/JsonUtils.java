package org.ironrhino.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.metadata.NotInJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static DateFormat[] ACCEPT_DATE_FORMATS = {
			new SimpleDateFormat(DEFAULT_DATE_FORMAT),
			new SimpleDateFormat("yyyy/MM/dd"),
			new SimpleDateFormat("yyyy-MM-dd") };

	private static Logger log = LoggerFactory.getLogger(JsonUtils.class);

	private static ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
		SerializationConfig config = objectMapper.getSerializationConfig();
		config = config.withSerializationInclusion(Inclusion.NON_NULL)
				.withAnnotationIntrospector(
						new JacksonAnnotationIntrospector() {

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
				.without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
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

	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, TypeReference<T> type)
			throws Exception {
		return (T) objectMapper.readValue(json, type);
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, Class<T> cls) throws Exception {
		if (Date.class.isAssignableFrom(cls)) {
			if (StringUtils.isNumericOnly(json)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.valueOf(json));
				return (T) cal.getTime();
			} else if (json.startsWith("\"") && json.endsWith("\"")) {
				String value = json.substring(1, json.length() - 2);
				for (DateFormat format : ACCEPT_DATE_FORMATS) {
					try {
						return (T) format.parse(value);
					} catch (Exception e) {
						continue;
					}
				}
			}
			return null;
		} else {
			return objectMapper.readValue(json, cls);
		}
	}

	public static <T> T fromJson(String json, Type type) throws Exception {
		return objectMapper.readValue(json, objectMapper
				.getDeserializationConfig().getTypeFactory()
				.constructType(type));
	}

	public static void main(String[] args) throws Exception {
		String json = "[1,2,{\"test\":\"test\"}]";
		JsonNode node = getObjectMapper().readValue(json, JsonNode.class);
		System.out.println(node.get(0).isContainerNode());
		System.out.println(node.get(2).isContainerNode());
		System.out.println(fromJson("\"2\"", Integer.class));
	}

}
