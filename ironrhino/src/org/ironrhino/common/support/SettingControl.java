package org.ironrhino.common.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Order;
import org.ironrhino.common.model.Setting;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.service.BaseManager;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("settingControl")
public class SettingControl implements ApplicationListener {

	protected Log log = LogFactory.getLog(getClass());

	private Map<String, Setting> settings;

	/**
	 * Autowired will cause exception when
	 * org.ironrhino.online.servlet.AccountAuthenticationProcessingFilter
	 * .save(final LoginRecord loginRecord),bloody weird
	 */
	private BaseManager<Setting> baseManager;

	public void setBaseManager(BaseManager<Setting> baseManager) {
		this.baseManager = baseManager;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		refresh();
	}

	public void refresh() {
		baseManager.setEntityClass(Setting.class);
		List<Setting> list = baseManager.findAll(Order.asc("key"));
		settings = new ConcurrentHashMap<String, Setting>();
		for (Setting s : list)
			settings.put(s.getKey(), s);
	}

	public Setting get(String key) {
		return settings.get(key);
	}

	public int getIntValue(String key) {
		return getIntValue(key, 0);
	}

	public int getIntValue(String key, int defaultValue) {
		Setting s = settings.get(key);
		if (s != null && StringUtils.isNotBlank(s.getValue()))
			return Integer.parseInt(s.getValue().trim());
		return defaultValue;
	}

	public boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	public boolean getBooleanValue(String key, boolean defaultValue) {
		Setting s = settings.get(key);
		if (s != null && StringUtils.isNotBlank(s.getValue()))
			return Boolean.parseBoolean(s.getValue().trim());
		return defaultValue;
	}

	public String getStringValue(String key) {
		return getStringValue(key, "");
	}

	public String getStringValue(String key, String defaultValue) {
		Setting s = settings.get(key);
		if (s != null && StringUtils.isNotBlank(s.getValue()))
			return s.getValue().trim();
		return defaultValue;
	}

	public String[] getStringArray(String key) {
		Setting s = settings.get(key);
		if (s != null && StringUtils.isNotBlank(s.getValue()))
			return s.getValue().trim().split(",");
		return new String[0];
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof EntityOperationEvent) {
			EntityOperationEvent ev = (EntityOperationEvent) event;
			if (ev.getEntity() instanceof Setting) {
				Setting s = (Setting) ev.getEntity();
				if (ev.getType() == EntityOperationType.CREATE) {
					settings.put(s.getKey(), s);
				} else {
					Setting ss = settings.get(s.getKey());
					if (ss != null)
						if (ev.getType() == EntityOperationType.UPDATE)
							BeanUtils.copyProperties(s, ss);
						else if (ev.getType() == EntityOperationType.DELETE)
							settings.remove(ss.getKey());
				}
			}
		}
	}
}
