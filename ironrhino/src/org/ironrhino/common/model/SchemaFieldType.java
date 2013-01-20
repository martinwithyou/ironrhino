package org.ironrhino.common.model;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public enum SchemaFieldType {
	SELECT, CHECKBOX, INPUT, GROUP;
	public String getName() {
		return name();
	}

	public String getDisplayName() {
		try {
			return LocalizedTextUtil.findText(getClass(), name(), ActionContext
					.getContext().getLocale(), name(), null);
		} catch (Exception e) {
			return name();
		}
	}

	public static SchemaFieldType parse(String name) {
		if (name != null)
			for (SchemaFieldType en : values())
				if (name.equals(en.name()) || name.equals(en.getDisplayName()))
					return en;
		return null;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
