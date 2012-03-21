package org.ironrhino.common.support;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Order;
import org.ironrhino.common.model.Dictionary;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.model.LabelValue;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		for (Dictionary d : list)
			temp.put(d.getName(), d);
		map = temp;
	}

	public Dictionary getDictionary(String name) {
		return map.get(name);
	}

	public Map<String, String> getItems(String name) {
		Dictionary dict = map.get(name);
		if (dict == null)
			return Collections.EMPTY_MAP;
		List<LabelValue> items = dict.getItems();
		Map<String, String> map = new LinkedHashMap<String, String>(
				items.size(), 1);
		for (LabelValue lv : items)
			map.put(lv.getValue(), lv.getLabel());
		return map;
	}

	public Map<String, Map<String, String>> getGroupedItems(String name) {
		Dictionary dict = map.get(name);
		if (dict == null)
			return Collections.EMPTY_MAP;
		List<LabelValue> items = dict.getItems();
		Set<String> groups = new LinkedHashSet<String>();
		for (LabelValue item : items) {
			String group = item.getGroup();
			if (StringUtils.isBlank(group))
				group = "";
			else
				group = group.trim();
			groups.add(group);
		}
		Map<String, Map<String, String>> groupedItems = new LinkedHashMap<String, Map<String, String>>(
				groups.size(), 1);
		for (String g : groups) {
			Iterator<LabelValue> it = items.iterator();
			while (it.hasNext()) {
				LabelValue item = it.next();
				String group = item.getGroup();
				if (StringUtils.isBlank(group))
					group = "";
				else
					group = group.trim();
				if (g.equals(group)) {
					Map<String, String> map = groupedItems.get(group);
					if (map == null) {
						map = new LinkedHashMap<String, String>();
						groupedItems.put(group, map);
					}
					map.put(item.getValue(), item.getLabel());
					it.remove();
				}
			}
		}
		return groupedItems;
	}

	public void onApplicationEvent(EntityOperationEvent event) {
		if (event.getEntity() instanceof Dictionary) {
			Dictionary dictInEvent = (Dictionary) event.getEntity();
			if (event.getType() == EntityOperationType.CREATE) {
				map.put(dictInEvent.getName(), dictInEvent);
			} else {
				Dictionary dictInMemory = null;
				for (Dictionary dictionary : map.values()) {
					if (dictionary.getId().equals(dictInEvent.getId())) {
						dictInMemory = dictionary;
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
