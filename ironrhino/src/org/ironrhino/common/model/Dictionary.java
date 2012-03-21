package org.ironrhino.common.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.aop.PublishAware;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.LabelValue;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.model.UserRole;

@PublishAware
@AutoConfig(searchable = true, order = "name asc")
@Searchable(alias = "dictionary")
@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
public class Dictionary extends BaseEntity {

	private static final long serialVersionUID = -8352037604261222984L;

	@NaturalId(caseInsensitive = true, mutable = true)
	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 1)
	private String name;

	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 2)
	private String description;

	@SearchableComponent
	// @UiConfig(displayOrder = 3, cellEdit = "none", excludeIfNotEdited = true,
	// template = "${entity.getItemsAsString()!}")
	@UiConfig(displayOrder = 3, hideInList = true)
	private List<LabelValue> items = new ArrayList<LabelValue>();

	private Map<String, List<LabelValue>> groupedItems;

	public Dictionary() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<LabelValue> getItems() {
		return items;
	}

	public void setItems(List<LabelValue> items) {
		groupedItems = null;
		this.items = items;
	}

	@NotInCopy
	public String getItemsAsString() {
		if (items == null || items.isEmpty())
			return null;
		return JsonUtils.toJson(items);
	}

	public void setItemsAsString(String str) {
		groupedItems = null;
		if (StringUtils.isNotBlank(str))
			try {
				items = JsonUtils.fromJson(str,
						new TypeReference<List<LabelValue>>() {
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@UiConfig(hide = true)
	@NotInCopy
	public Map<String, List<LabelValue>> getGroupedItems() {
		if (groupedItems == null) {
			Set<String> groups = new LinkedHashSet<String>();
			for (LabelValue item : items) {
				String group = item.getGroup();
				if (StringUtils.isBlank(group))
					group = "";
				else
					group = group.trim();
				groups.add(group);
			}
			groupedItems = new LinkedHashMap<String, List<LabelValue>>(
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
						List<LabelValue> list = groupedItems.get(group);
						if (list == null) {
							list = new ArrayList<LabelValue>();
							groupedItems.put(group, list);
						}
						list.add(item);
						it.remove();
					}
				}
			}
		}
		return groupedItems;
	}

}
