package org.ironrhino.common.model;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ironrhino.core.dataroute.DataRoute;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.stat.Key;
import org.ironrhino.core.stat.KeyValuePair;
import org.ironrhino.core.stat.Value;

@DataRoute("miscGroup")
@AutoConfig(readonly = true)
public class Stat extends KeyValuePair implements Persistable<String> {

	private static final long serialVersionUID = -1795832273603877285L;

	private String id;

	@UiConfig(hidden = true)
	private String keyAsString;

	@UiConfig(hidden = true)
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
		return StringUtils.isNotBlank(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	@UiConfig(hidden = true)
	public Key getKey() {
		return key;
	}

	@Override
	@UiConfig(hidden = true)
	public Value getValue() {
		return value;
	}

	@UiConfig(displayOrder = 0, displayName = "key")
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

	@UiConfig(displayOrder = 1, displayName = "value")
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
	@UiConfig(displayOrder = 2)
	public Date getDate() {
		return date;
	}

	@Override
	@UiConfig(displayOrder = 3)
	public String getHost() {
		return host;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
