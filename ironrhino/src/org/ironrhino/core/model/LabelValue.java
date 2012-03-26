package org.ironrhino.core.model;

import java.io.Serializable;

import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false)
public class LabelValue implements Serializable {

	private static final long serialVersionUID = 7629652470042630809L;

	@SearchableProperty(boost = 2, index = Index.NOT_ANALYZED)
	private String value;

	@SearchableProperty(boost = 2, index = Index.NOT_ANALYZED)
	private String label;

	private Boolean selected;

	public LabelValue() {
	}

	public LabelValue(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

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
