package org.ironrhino.core.dataroute;

public class DataRouteContext {

	public static final int DEFAULT_DATASOURCE_WEIGHT = 1;

	private static ThreadLocal<Boolean> readonly = new ThreadLocal<Boolean>();

	private static ThreadLocal<String> groupName = new ThreadLocal<String>();

	public static void setReadonly(boolean bl) {
		readonly.set(bl);
	}

	public static boolean isReadonly() {
		Boolean bl = readonly.get();
		return bl != null && bl.booleanValue();
	}

	public static void setGroupName(String s) {
		groupName.set(s);
	}

	public static String getGroupName() {
		return groupName.get();
	}

}