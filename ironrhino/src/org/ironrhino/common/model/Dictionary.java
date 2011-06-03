package org.ironrhino.common.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.LabelValue;
import org.ironrhino.core.util.JsonUtils;

import com.opensymphony.xwork2.util.CreateIfNull;

@PublishAware
@AutoConfig(searchable = true, order = "name asc")
@Searchable(alias = "dictionary")
public class Dictionary extends BaseEntity {

	private static final long serialVersionUID = -8352037604261222984L;

	@NaturalId(caseInsensitive = true, mutable = true)
	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 1, cellEdit = "none")
	private String name;

	@SearchableComponent
	@UiConfig(displayOrder = 2, cellEdit = "none",template = "{<#assign index=0><#list value as item>${item.label}:${item.value}<#assign index=index+1><#if index!=value?size>,</#if></#list>}")
	private List<LabelValue> items = new ArrayList<LabelValue>();

	public Dictionary() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<LabelValue> getItems() {
		return items;
	}

	@CreateIfNull
	public void setItems(List<LabelValue> items) {
		this.items = items;
	}

	@NotInCopy
	public String getItemsAsString() {
		if (items == null || items.isEmpty())
			return null;
		return JsonUtils.toJson(items);
	}

	public void setItemsAsString(String str) {
		if (StringUtils.isNotBlank(str))
			try {
				items = JsonUtils.fromJson(str,
						new TypeReference<List<LabelValue>>() {
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
