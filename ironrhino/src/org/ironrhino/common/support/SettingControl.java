package org.ironrhino.common.support;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component("settingControl")
public class SettingControl {

	protected Log log = LogFactory.getLog(getClass());

	private Map<String, String> settings = new TreeMap<String, String>();

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		load();
	}

	public void load() {
		settings.clear();
		// TODO init
		settings.put("product.recommanded", "productCode100,productCode200,productCode300,productCode400");
		settings.put("haha", "hehe");
		settings.put("haha1", "hehe");
		settings.put("haha2", "hehe");
		settings.put("haha3", "hehe");
	}

	public Map<String, String> getAll() {
		return settings;
	}

	public void set(String key, String value) {
		settings.put(key, value);
	}

	public int getIntValue(String key) {
		return getIntValue(key, 0);
	}

	public int getIntValue(String key, int defaultValue) {
		String s = settings.get(key);
		if (StringUtils.isNotBlank(s))
			return Integer.parseInt(s.trim());
		return defaultValue;
	}

	public long getLongValue(String key) {
		return getLongValue(key, 0);
	}

	public long getLongValue(String key, long defaultValue) {
		String s = settings.get(key);
		if (StringUtils.isNotBlank(s))
			return Long.parseLong(s.trim());
		return defaultValue;
	}

	public double geDoubleValue(String key) {
		return getDoubleValue(key, 0);
	}

	public double getDoubleValue(String key, double defaultValue) {
		String s = settings.get(key);
		if (StringUtils.isNotBlank(s))
			return Double.parseDouble(s.trim());
		return defaultValue;
	}

	public boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	public boolean getBooleanValue(String key, boolean defaultValue) {
		String s = settings.get(key);
		if (StringUtils.isNotBlank(s))
			return Boolean.parseBoolean(s.trim());
		return defaultValue;
	}

	public String getStringValue(String key) {
		return getStringValue(key, "");
	}

	public String getStringValue(String key, String defaultValue) {
		String s = settings.get(key);
		if (StringUtils.isNotBlank(s))
			return s.trim();
		return defaultValue;
	}

	public String[] getStringArray(String key) {
		String s = settings.get(key);
		if (StringUtils.isNotBlank(s))
			return org.springframework.util.StringUtils
					.commaDelimitedListToStringArray(s);
		return new String[0];
	}

}
