package org.ironrhino.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AppInfo {

	public static final String KEY_STAGE = "STAGE";

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_APP_HOME = "app.home";

	public static final String KEY_APP_VERSION = "app.version";

	public static final String KEY_RACK = "RACK";

	public static String name = "app";

	public static String home;

	public static String version = "1.0.0";

	public static final Stage STAGE;

	public static final String HOSTNAME;

	public static final String HOSTADDRESS;

	public static final String DEFAULT_RACK = "/default-rack";

	public static final String NODEPATH;

	static {
		String stage = System.getProperty(KEY_STAGE);
		if (stage == null)
			stage = System.getenv(KEY_STAGE);
		Stage s = null;
		if (stage != null)
			try {
				s = Stage.valueOf(stage.toUpperCase());
			} catch (Exception e) {
			}
		if (s != null)
			STAGE = s;
		else
			STAGE = Stage.PRODUCTION;

		String name = "unknown";
		String address = "";
		try {
			name = InetAddress.getLocalHost().getHostName();
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		HOSTNAME = name;
		HOSTADDRESS = address;

		String rack = System.getProperty(KEY_RACK);
		if (rack == null)
			rack = System.getenv(KEY_RACK);
		if (rack == null)
			rack = DEFAULT_RACK;
		StringBuilder sb = new StringBuilder();
		if (!rack.startsWith("/"))
			sb.append("/");
		sb.append(rack);
		if (!rack.endsWith("/"))
			sb.append("/");
		sb.append(HOSTNAME);
		NODEPATH = sb.toString();
	}

	public static void setName(String name) {
		AppInfo.name = name;
	}

	public static void setHome(String home) {
		AppInfo.home = home;
	}

	public static void setVersion(String version) {
		AppInfo.version = version;
	}

	public static Stage getStage() {
		return STAGE;
	}

	public static String getAppHome() {
		if (home != null)
			return home;
		else
			return System.getProperty("user.home").replace('\\', '/') + "/"
					+ name;
	}

	public static String getHostName() {
		return HOSTNAME;
	}

	public static String getHostAddress() {
		return HOSTADDRESS;
	}

	public static String getNodePath() {
		return NODEPATH;
	}

	public static enum Stage {
		DEVELOPMENT, TEST, PREPARATION, PRODUCTION
	}

}