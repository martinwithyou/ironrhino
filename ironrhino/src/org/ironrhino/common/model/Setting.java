package org.ironrhino.common.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;

@RecordAware
@PublishAware
@AutoConfig
public class Setting extends BaseEntity {

	@NaturalId
	private String key = "";

	private String value = "";

	public Setting() {

	}

	public Setting(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
