package org.ironrhino.core.search;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.compass.core.CompassQuery.SortDirection;
import org.compass.core.CompassQuery.SortPropertyType;

class CompassSort {

	private String name;

	private SortPropertyType type;

	private SortDirection direction;

	public CompassSort(String sortParamName, SortPropertyType paramType,
			boolean isDescend) {
		setName(sortParamName);
		setType(paramType != null ? paramType : SortPropertyType.AUTO);
		setDirection(isDescend ? SortDirection.REVERSE : SortDirection.AUTO);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SortPropertyType getType() {
		return type;
	}

	public void setType(SortPropertyType type) {
		this.type = type;
	}

	public SortDirection getDirection() {
		return direction;
	}

	public void setDirection(SortDirection direction) {
		this.direction = direction;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object object) {
		return EqualsBuilder.reflectionEquals(this, object);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
