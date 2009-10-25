package org.ironrhino.core.util;

public class AppInfo {

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_APP_VERSION = "app.version";

	public static String APP_NAME = "app";

	public static String APP_VERSION = "1.0.0";

	public static String getAppHome() {
		return System.getProperty("user.home") + "/" + APP_NAME;
	}

	public static void setAppName(String name) {
		APP_NAME = name;
	}

	public static void setAppVersion(String version) {
		APP_VERSION = version;
	}

}
