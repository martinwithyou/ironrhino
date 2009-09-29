package org.ironrhino.core.hibernate;

public class PropertyChange {

	private String name;

	private boolean remove;

	private PropertyType type = PropertyType.STRING;

	public PropertyChange() {

	}

	public PropertyChange(String name) {
		super();
		this.name = name;
	}

	public PropertyChange(String name, boolean remove) {
		super();
		this.name = name;
		this.remove = remove;
	}

	public PropertyChange(String name, boolean remove, PropertyType type) {
		super();
		this.name = name;
		this.remove = remove;
		this.type = type;
	}

	public boolean isRemove() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PropertyType getType() {
		return type;
	}

	public void setType(PropertyType type) {
		this.type = type;
	}

}
