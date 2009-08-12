package org.ironrhino.common.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.annotation.FormElement;
import org.ironrhino.core.model.BaseEntity;

@AutoConfig(readonly = true)
public class Stat extends BaseEntity {

	@FormElement(displayOrder = 0)
	private String key;

	@FormElement(displayOrder = 1)
	private long longValue;

	@FormElement(displayOrder = 2)
	private double doubleValue;

	@FormElement(displayOrder = 3)
	private Date statDate;

	public Stat() {

	}

	public Stat(String key, long longValue, double doubleValue, Date statDate) {
		super();
		this.key = key;
		this.longValue = longValue;
		this.doubleValue = doubleValue;
		this.statDate = statDate;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Date getStatDate() {
		return statDate;
	}

	public void setStatDate(Date statDate) {
		this.statDate = statDate;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
