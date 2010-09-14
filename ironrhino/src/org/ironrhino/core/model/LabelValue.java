package org.ironrhino.core.model;

import java.io.Serializable;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false, alias = "labelValue")
public class LabelValue implements Serializable {

	private String label;

	private String value;

	public LabelValue() {
	}

	public LabelValue(String name, String value) {
		this.label = name;
		this.value = value;
	}

	public LabelValue(String name) {
		this.label = name;
	}

	@SearchableProperty
	public String getLabel() {
		return label;
	}

	public void setLabel(String name) {
		this.label = name;
	}

	@SearchableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
