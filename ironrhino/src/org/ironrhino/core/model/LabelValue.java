package org.ironrhino.core.model;

import java.io.Serializable;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false, alias = "labelValue")
public class LabelValue implements Serializable {

	private String value;

	private String label;

	private Boolean selected;

	public LabelValue() {
	}

	public LabelValue(String value, String label) {
		this.value = value;
		this.label = label;
	}

	@SearchableProperty
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@SearchableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

}
