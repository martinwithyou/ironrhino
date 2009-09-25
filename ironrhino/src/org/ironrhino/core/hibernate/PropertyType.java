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
}
