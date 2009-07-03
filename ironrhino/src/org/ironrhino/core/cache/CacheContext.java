package org.ironrhino.core.cache;

import org.apache.struts2.ServletActionContext;

public class CacheContext {

	public static final String FORCE_FLUSH_PARAM_NAME = "_ff_";

	public static boolean forceFlush() {
		try {
			return ServletActionContext.getRequest() != null
					&& ServletActionContext.getRequest().getParameter(
							FORCE_FLUSH_PARAM_NAME) != null;
		} catch (Exception e) {
			return false;
		}
	}
}
