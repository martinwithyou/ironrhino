package com.ironrhino.online.model;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public enum FeedbackStatus {

	UNDECIDED, IGNORED, ACCEPTED, RESOLVED;

	public String getName() {
		return this.name();
	}

	public String getDisplayName() {
		return LocalizedTextUtil.findText(getClass(), name(), ActionContext
				.getContext().getLocale(), name(), null);
	}

	public static FeedbackStatus parse(String name) {
		if (name != null)
			for (FeedbackStatus en : values())
				if (name.equals(en.name()) || name.equals(en.getDisplayName()))
					return en;
		return null;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
