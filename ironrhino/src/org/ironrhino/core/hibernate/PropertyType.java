package org.ironrhino.core.hibernate;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public enum PropertyType {

	STRING, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BIGDECIMAL, BOOLEAN, DATE;

	public String getName() {
		return this.name();
	}

	public String getDisplayName() {
		return LocalizedTextUtil.findText(getClass(), name(), ActionContext
				.getContext().getLocale(), name(), null);
	}

	public static PropertyType parse(String name) {
		if (name != null)
			for (PropertyType en : values())
				if (name.equals(en.name()) || name.equals(en.getDisplayName()))
					return en;
		return null;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
