package org.ironrhino.online.model;

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
}
