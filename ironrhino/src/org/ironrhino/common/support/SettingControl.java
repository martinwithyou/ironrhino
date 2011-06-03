package org.ironrhino.common.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Order;
import org.ironrhino.common.model.Setting;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.service.BaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationListener;

@Singleton
@Named("settingControl")
public class SettingControl implements
		ApplicationListener<EntityOperationEvent> {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Setting> settings;

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
		Map<String, Setting> temp = new ConcurrentHashMap<String, Setting>(
				list.size(), 1);
		for (Setting s : list)
			temp.put(s.getKey(), s);
		settings = temp;
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

	public void onApplicationEvent(EntityOperationEvent event) {
		if (event.getEntity() instanceof Setting) {
			Setting settingInEvent = (Setting) event.getEntity();
			if (event.getType() == EntityOperationType.CREATE) {
				settings.put(settingInEvent.getKey(), settingInEvent);
			} else {
				Setting settingInMemory = null;
				for (Setting setting : settings.values()) {
					if (setting.getId().equals(settingInEvent.getId())) {
						settingInMemory = setting;
						break;
					}
				}
				if (settingInMemory != null)
					if (event.getType() == EntityOperationType.UPDATE) {
						settings.remove(settingInMemory.getKey());
						BeanUtils.copyProperties(settingInEvent,
								settingInMemory);
						settings.put(settingInMemory.getKey(), settingInMemory);
					} else if (event.getType() == EntityOperationType.DELETE) {
						settings.remove(settingInMemory.getKey());
					}
			}
		}
	}
}
