package org.ironrhino.common.model;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public enum Status {
	REQUESTED, CONFIRMED, CANCELLED;
	public String getName() {
		return name();
	}

	public String getDisplayName() {
		return LocalizedTextUtil.findText(getClass(), name(), ActionContext
				.getContext().getLocale(), name(), null);
	}

	public static Status parse(String name) {
		if (name != null)
			for (Status en : values())
				if (name.equals(en.name()) || name.equals(en.getDisplayName()))
					return en;
		return null;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
