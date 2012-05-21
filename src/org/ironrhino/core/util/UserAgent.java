package org.ironrhino.core.util;

import java.io.Serializable;

public class UserAgent implements Serializable {

	private static final long serialVersionUID = 7964528679540704029L;

	private String userAgent;

	private String name = "unknown";

	private String platform = "unknown";

	private String version = "unknown";

	private int majorVersion;

	private int minorVersion;

	public UserAgent() {

	}

	public UserAgent(String userAgent) {
		setUserAgent(userAgent);
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		if (userAgent != null) {
			String lower = userAgent.toLowerCase();
			if (lower.contains("windows")) {
				platform = "windows";
			} else if (lower.contains("linux")) {
				platform = "linux";
			} else if (lower.contains("os x")) {
				platform = "osx";
			} else if (lower.contains("android")) {
				platform = "android";
			} else if (lower.contains("iphone") || lower.contains("ios")) {
				platform = "ios";
			}
			if (lower.contains("webkit")) {
				name = "webkit";
			} else if (userAgent.contains("Opera")) {
				name = "opera";
			} else if (userAgent.contains("MSIE")) {
				name = "msie";
				String str = "MSIE";
				int index = userAgent.indexOf(str) + str.length() + 1;
				version = userAgent.substring(index, userAgent.indexOf(";",
						index));
			} else if (userAgent.contains("Mozilla")) {
				name = "mozilla";
			}
			setVersion(version);
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		if (version != null && !version.equals("unknown")) {
			String[] arr = version.split("\\.");
			try {
				majorVersion = Integer.parseInt(arr[0]);
				if (arr.length > 1)
					minorVersion = Integer.parseInt(arr[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public String getPlatform() {
		return platform;
	}

}
