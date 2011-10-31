package org.ironrhino.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AppInfo {

	public static final String KEY_STAGE = "STAGE";

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_APP_HOME = "app.home";

	public static final String KEY_APP_VERSION = "app.version";

	public static final String KEY_RACK = "RACK";

	public static final String DEFAULT_RACK = "/default-rack";

	private static String name = "app";

	private static String _instanceId;

	private static String home;

	private static String version = "1.0.0";

	private static final Stage STAGE;

	private static final String HOSTNAME;

	private static final String HOSTADDRESS;

	private static final String NODEPATH;

	static {
		_instanceId = CodecUtils.nextId();
		String stage = getEnv(KEY_STAGE);
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
			InetAddress localhost = InetAddress.getLocalHost();
			name = localhost.getHostName();
			address = localhost.getHostAddress();
		} catch (UnknownHostException e) {
		}
		HOSTNAME = name;
		HOSTADDRESS = address;

		String rack = getEnv(KEY_RACK);
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

	public static void setAppName(String name) {
		AppInfo.name = name;
	}

	public static void setAppHome(String home) {
		AppInfo.home = home;
	}

	public static void setAppVersion(String version) {
		AppInfo.version = version;
	}

	public static Stage getStage() {
		return STAGE;
	}

	public static String getAppName() {
		return name;
	}

	public static String getInstanceId() {
		return getAppName() + "-" + _instanceId;
	}

	public static String getAppVersion() {
		return version;
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

	private static String getEnv(String key) {
		String value = System.getProperty(key);
		if (value == null)
			value = System.getenv(key);
		return value;
	}

	public static enum Stage {
		DEVELOPMENT, TEST, PREPARATION, PRODUCTION
	}

}
