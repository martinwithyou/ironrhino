package org.ironrhino.common.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.model.BaseEntity;

@AutoConfig
public class Stat extends BaseEntity {

	private String key;

	private long longValue;

	private double doubleValue;

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
