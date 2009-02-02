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
	
	public String toString(){
		return getDisplayName();
	}
}
