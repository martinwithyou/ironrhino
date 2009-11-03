package org.ironrhino.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AppInfo {

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_APP_VERSION = "app.version";

	public static String APP_NAME = "app";

	public static String APP_VERSION = "1.0.0";

	public static final String HOSTNAME;

	public static final String DEFAULT_RACK = "/default-rack";

	public static final String NODEPATH;

	static {
		String name = "unknown";
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		HOSTNAME = name;
		String[] array = HOSTNAME.split("-");
		StringBuilder sb = new StringBuilder();
		if (array.length == 1) {
			sb.append(DEFAULT_RACK);
			sb.append('/');
			sb.append(array[0]);
		} else {
			for (int i = 0; i < array.length; i++) {
				sb.append('/');
				sb.append(array[i]);
			}
		}
		NODEPATH = sb.toString();
	}

	public static String getAppHome() {
		return System.getProperty("user.home") + "/" + APP_NAME;
	}

	public static void setAppName(String name) {
		APP_NAME = name;
	}

	public static void setAppVersion(String version) {
		APP_VERSION = version;
	}

	public static String getHostName() {
		return HOSTNAME;
	}

	public static String getNodePath() {
		return NODEPATH;
	}

}
