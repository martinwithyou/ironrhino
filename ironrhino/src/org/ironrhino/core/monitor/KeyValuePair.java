package org.ironrhino.core.monitor;

import java.io.Serializable;
import java.util.Date;

public class KeyValuePair implements Serializable {

	private Key key;

	private Value value;

	private Date date;

	public KeyValuePair() {

	}

	public KeyValuePair(Key key, Value value, Date date) {
		super();
		this.key = key;
		this.value = value;
		this.date = date;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
