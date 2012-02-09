package org.ironrhino.common.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
	//@UiConfig(displayOrder = 3, cellEdit = "none", excludeIfNotEdited = true, template = "${entity.getItemsAsString()!}")
	@UiConfig(displayOrder = 3, hideInList = true)
	private List<LabelValue> items = new ArrayList<LabelValue>();

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
		this.items = items;
	}

	@NotInCopy
	public String getItemsAsString() {
		if (items == null || items.isEmpty())
			return null;
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (LabelValue lv : items)
			map.put(lv.getValue(), lv.getLabel());
		return JsonUtils.toJson(map);
	}

	public void setItemsAsString(String str) {
		if (StringUtils.isNotBlank(str))
			try {
				Map<String, String> map = JsonUtils.fromJson(str,
						new TypeReference<Map<String, String>>() {
						});
				items = new ArrayList<LabelValue>(map.size());
				for (Map.Entry<String, String> entry : map.entrySet())
					items.add(new LabelValue(entry.getKey(), entry.getValue()));
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
