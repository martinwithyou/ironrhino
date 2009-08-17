package org.ironrhino.common.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.FormElement;
import org.ironrhino.core.metadata.Readonly;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.Value;

@Readonly
@AutoConfig
public class Stat extends BaseEntity {

	private static final long serialVersionUID = 5933509155833960220L;

	@FormElement(displayOrder = 0)
	private String key;

	@FormElement(displayOrder = 1)
	private long longValue;

	@FormElement(displayOrder = 2)
	private double doubleValue;

	@FormElement(displayOrder = 3)
	private Date statDate;

	@FormElement(displayOrder = 4)
	private String host;

	public Stat() {

	}

	public Stat(String key, long longValue, double doubleValue, Date statDate,
			String host) {
		super();
		this.key = key;
		this.longValue = longValue;
		this.doubleValue = doubleValue;
		this.statDate = statDate;
		this.host = host;
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public KeyValuePair toKeyValuePair() {
		return new KeyValuePair(Key.fromString(getKey()), new Value(longValue,
				doubleValue), statDate);
	}
}
