package org.ironrhino.core.model;

import java.io.Serializable;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false, alias = "attribute")
public class Attribute implements Serializable {

	private static final long serialVersionUID = 3709022318256011161L;

	private String name;

	private String value;

	public Attribute() {
	}

	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public Attribute(String name) {
		this.name = name;
	}

	@SearchableProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SearchableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
