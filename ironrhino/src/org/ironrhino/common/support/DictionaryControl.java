package org.ironrhino.common.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.ironrhino.common.model.Dictionary;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.model.LabelValue;
import org.ironrhino.core.service.BaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ironrhino.core.util.BeanUtils;
import org.springframework.context.ApplicationListener;

@Singleton
@Named("dictionaryControl")
public class DictionaryControl implements
		ApplicationListener<EntityOperationEvent> {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Dictionary> map;

	private BaseManager<Dictionary> baseManager;

	public void setBaseManager(BaseManager<Dictionary> baseManager) {
		this.baseManager = baseManager;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		refresh();
	}

	public void refresh() {
		baseManager.setEntityClass(Dictionary.class);
		List<Dictionary> list = baseManager.findAll(Order.asc("name"));
		Map<String, Dictionary> temp = new ConcurrentHashMap<String, Dictionary>(
				list.size(), 1);
		for (Dictionary s : list)
			temp.put(s.getName(), s);
		map = temp;
	}

	public List<LabelValue> getItems(String name) {
		Dictionary dict = map.get(name);
		return dict != null ? dict.getItems() : Collections.EMPTY_LIST;
	}

	public void onApplicationEvent(EntityOperationEvent event) {
		if (event.getEntity() instanceof Dictionary) {
			Dictionary dictInEvent = (Dictionary) event.getEntity();
			if (event.getType() == EntityOperationType.CREATE) {
				map.put(dictInEvent.getName(), dictInEvent);
			} else {
				Dictionary dictInMemory = null;
				for (Dictionary Dictionary : map.values()) {
					if (Dictionary.getId().equals(dictInEvent.getId())) {
						dictInMemory = Dictionary;
						break;
					}
				}
				if (dictInMemory != null)
					if (event.getType() == EntityOperationType.UPDATE) {
						map.remove(dictInMemory.getName());
						BeanUtils.copyProperties(dictInEvent, dictInMemory);
						map.put(dictInMemory.getName(), dictInMemory);
					} else if (event.getType() == EntityOperationType.DELETE) {
						map.remove(dictInMemory.getName());
					}
			}
		}
	}
}
