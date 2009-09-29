package org.ironrhino.core.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.ironrhino.core.metadata.JsonSerializerType;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.util.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

public class JsonUtils {

	private static Gson gson = new Gson();

	public static String mapToJson(Map<Object, Object> map) {
		if (map == null)
			return null;
		JSONObject jo = new JSONObject();
		try {
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				String key = entry.getKey().toString();
				Object value = map.get(key);
				if (isSimple(value))
					jo.put(key, simpleObjectToJSON(value));
				else if (isArray(value)) {
					jo.put(key, arrayObjectToJSON(value));
				} else if (isMap(value)) {
					jo.put(key, mapObjectToJSON(value));
				} else {
					jo.put(key, complexObjectToJson(value));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jo.toString();
	}

	public static String toJson(Object object) {
		if (object == null)
			return "";
		if (isSimple(object))
			return simpleObjectToJSON(object).toString();
		else if (isArray(object)) {
			return arrayObjectToJSON(object).toString();
		} else if (isMap(object)) {
			return mapObjectToJSON(object).toString();
		} else {
			return complexObjectToJson(object).toString();
		}
	}

	public static String toJson(Object object, JsonSerializerType type) {
		if (type == JsonSerializerType.GSON)
			return gson.toJson(object);
		return toJson(object);
	}

	public static JSONObject complexObjectToJson(Object o) {
		if (o == null)
			return null;
		Set<String> ignoreProperties = AnnotationUtils
				.getAnnotatedPropertyNames(o.getClass(), NotInJson.class);
		JSONObject jo = new JSONObject();
		try {

			PropertyDescriptor[] pds = Introspector.getBeanInfo(o.getClass())
					.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds)
				if (pd.getReadMethod() != null && !pd.getName().equals("class")
						&& !ignoreProperties.contains(pd.getName())) {
					Object value = pd.getReadMethod()
							.invoke(o, new Object[] {});
					if (isSimple(value))
						jo.put(pd.getName(), simpleObjectToJSON(value));
					else if (isArray(value)) {
						jo.put(pd.getName(), arrayObjectToJSON(value));
					} else if (isMap(value)) {
						jo.put(pd.getName(), mapObjectToJSON(value));
					} else {
						jo.put(pd.getName(), complexObjectToJson(value));
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jo;
	}

	public static JSONArray arrayObjectToJSON(Object o) {
		if (o == null || !isArray(o))
			return null;
		JSONArray ja = new JSONArray();
		Object[] array;
		try {
			if (o instanceof Collection) {
				array = ((Collection) o).toArray();
			} else {
				array = (Object[]) o;
			}
			for (int i = 0; i < array.length; i++) {
				if (isSimple(array[i])) {
					ja.put(simpleObjectToJSON(array[i]));
				} else if (isArray(array[i])) {
					ja.put(arrayObjectToJSON(array[i]));
				} else if (isMap(array[i])) {
					ja.put(mapObjectToJSON(array[i]));
				} else {
					ja.put(complexObjectToJson(array[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ja;
	}

	public static Object simpleObjectToJSON(Object o) {
		if (o instanceof Date)
			return DateUtils.getDatetime((Date) o);
		return o;
	}

	public static JSONObject mapObjectToJSON(Object o) {
		if (o == null)
			return null;
		JSONObject jo = new JSONObject();
		Map<Object, Object> map = (Map) o;
		try {
			for (Map.Entry entry : map.entrySet()) {
				String key = entry.getKey().toString();
				Object value = entry.getValue();
				if (isSimple(value))
					jo.put(key, simpleObjectToJSON(value));
				else if (isArray(value)) {
					jo.put(key, arrayObjectToJSON(value));
				} else if (isMap(value)) {
					jo.put(key, mapObjectToJSON(value));
				} else {
					jo.put(key, complexObjectToJson(value));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jo;
	}

	private static boolean isSimple(Object o) {
		if (o == null)
			return true;
		if (o instanceof Number)
			return true;
		if (o instanceof String)
			return true;
		if (o instanceof Boolean)
			return true;
		if (o instanceof Character)
			return true;
		if (o.getClass().isEnum())
			return true;
		if (o instanceof Date)
			return true;
		if (o instanceof JSONArray)
			return true;
		if (o instanceof JSONObject)
			return true;
		return false;
	}

	private static boolean isArray(Object o) {
		if (o == null)
			return false;
		if (o instanceof Collection)
			return true;
		if (o.getClass().isArray())
			return true;
		return false;
	}

	private static boolean isMap(Object o) {
		if (o == null)
			return false;
		if (o instanceof Map)
			return true;
		return false;
	}

	// private static boolean isComplex(Object o) {
	// return !(isSimple(o) || isArray(o) || isMap(o));
	// }

}
