package org.ironrhino.core.aspect;

public class AopContext {

	private static ThreadLocal<Class> bypass = new ThreadLocal<Class>();

	public static void setBypass(Class clazz) {
		bypass.set(clazz);
	}

	public static boolean isBypass(Class clazz) {
		boolean bl = (bypass.get() != null && bypass.get().equals(clazz));
		bypass.set(null);
		return bl;
	}

}
