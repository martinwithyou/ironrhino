package org.ironrhino.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false)
public class SchemaField implements Serializable {

	@SearchableProperty(boost = 2, index = Index.NOT_ANALYZED)
	private String name;

	@SearchableProperty(boost = 2, index = Index.NOT_ANALYZED)
	private List<String> values = new ArrayList<String>();

	private SchemaFieldType type;

	private boolean required;

	private boolean strict;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public SchemaFieldType getType() {
		return type;
	}

	public void setType(SchemaFieldType type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	public boolean equals(Object o) {
		if (o instanceof SchemaField) {
			SchemaField that = (SchemaField) o;
			return that.getName() != null
					&& that.getName().equals(this.getName());
		}
		return false;
	}

}
