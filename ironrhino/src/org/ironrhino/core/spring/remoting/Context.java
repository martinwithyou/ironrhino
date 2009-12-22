package org.ironrhino.core.spring.remoting;

import java.util.Map;

public class Context {

	public final static String KEY = "k";
	public final static String VERSION = "v";

	public static ThreadLocal<Map<String, String[]>> PARAMETERS_MAP = new ThreadLocal<Map<String, String[]>>();

	public static ThreadLocal<Class> SERVICE = new ThreadLocal<Class>();

	public static String get(String key) {
		Map<String, String[]> m = PARAMETERS_MAP.get();
		if (m == null)
			return null;
		else
			return m.get(key)[0];
	}

	public static void reset() {
		PARAMETERS_MAP.remove();
		SERVICE.remove();
	}
}
