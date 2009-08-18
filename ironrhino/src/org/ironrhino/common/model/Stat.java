package org.ironrhino.common.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.FormElement;
import org.ironrhino.core.metadata.NotInUI;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.Value;

@AutoConfig(readonly = true)
public class Stat extends KeyValuePair implements Persistable {

	private static final long serialVersionUID = -1795832273603877285L;

	private String id;

	@NotInUI
	private String keyAsString;

	@NotInUI
	private String valueAsString;

	public Stat() {

	}

	public Stat(Key key, Value value, Date date, String host) {
		super();
		this.key = key;
		this.value = value;
		this.date = date;
		this.host = host;
	}

	public boolean isNew() {
		return this.id != null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@FormElement(displayOrder = 0)
	public Key getKey() {
		return key;
	}

	@FormElement(displayOrder = 1)
	public Value getValue() {
		return value;
	}

	@FormElement(displayOrder = 2)
	public Date getDate() {
		return date;
	}

	@FormElement(displayOrder = 3)
	public String getHost() {
		return host;
	}

	public String getKeyAsString() {
		if (keyAsString != null)
			return keyAsString;
		if (key != null)
			return key.toString();
		return null;
	}

	public void setKeyAsString(String keyAsString) {
		this.keyAsString = keyAsString;
		if (key == null)
			key = Key.fromString(keyAsString);
	}

	public String getValueAsString() {
		if (valueAsString != null)
			return valueAsString;
		if (value != null)
			return value.toString();
		return null;
	}

	public void setValueAsString(String valueAsString) {
		this.valueAsString = valueAsString;
		if (value == null)
			value = Value.fromString(valueAsString);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
