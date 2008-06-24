package org.ironrhino.core.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.ironrhino.common.util.DateUtils;
import org.ironrhino.core.annotation.NotInJson;
import org.json.JSONArray;
import org.json.JSONObject;


public class JSONUtils {

	public static String mapToJSON(Map<String, ?> map) {
		if (map == null)
			return null;
		JSONObject jo = new JSONObject();
		try {
			for (String key : map.keySet()) {
				Object value = map.get(key);
				if (isSimple(value))
					jo.put(key, simpleObjectToJSON(value));
				else if (isArray(value)) {
					jo.put(key, arrayObjectToJSON(value));
				} else if (isMap(value)) {
					jo.put(key, mapObjectToJSON(value));
				} else {
					jo.put(key, complexObjectToJSON(value));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jo.toString();
	}

	public static String objectToJSON(Object object) {
		if(object==null)
			return "";
		if (isSimple(object))
			return simpleObjectToJSON(object).toString();
		else if (isArray(object)) {
			return arrayObjectToJSON(object).toString();
		} else if (isMap(object)) {
			return mapObjectToJSON(object).toString();
		} else {
			return complexObjectToJSON(object).toString();
		}
	}

	public static JSONObject complexObjectToJSON(Object o) {
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
						jo.put(pd.getName(), complexObjectToJSON(value));
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
					ja.put(complexObjectToJSON(array[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ja;
	}

	public static Object simpleObjectToJSON(Object o) {
		if (o instanceof Date)
			return DateUtils.getDate10((Date) o);
		return o;
	}

	public static JSONObject mapObjectToJSON(Object o) {
		if (o == null)
			return null;
		JSONObject jo = new JSONObject();
		Map map = (Map) o;
		try {
			for (Object key : map.keySet()) {
				Object value = map.get(key);
				if (isSimple(value))
					jo.put(key.toString(), simpleObjectToJSON(value));
				else if (isArray(value)) {
					jo.put(key.toString(), arrayObjectToJSON(value));
				} else if (isMap(value)) {
					jo.put(key.toString(), mapObjectToJSON(value));
				} else {
					jo.put(key.toString(), complexObjectToJSON(value));
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
