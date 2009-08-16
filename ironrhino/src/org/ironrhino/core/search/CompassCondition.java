package org.ironrhino.core.search;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

class CompassCondition {

	private String name;

	private Object value;

	private Object low;

	private Object high;

	private CompassConditionType type;

	public CompassCondition(String name, Object value, CompassConditionType type) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public CompassCondition(String name, Object low, Object high) {
		super();
		this.name = name;
		this.low = low;
		this.high = high;
		this.type = CompassConditionType.BETWEEN;
	}

	public Object getHigh() {
		return high;
	}

	public void setHigh(Object high) {
		this.high = high;
	}

	public Object getLow() {
		return low;
	}

	public void setLow(Object low) {
		this.low = low;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CompassConditionType getType() {
		return type;
	}

	public void setType(CompassConditionType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
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
