package org.ironrhino.ums.action;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;

@AutoConfig(namespace = "/")
public class SwitchUserAction extends BaseAction {

	private static final long serialVersionUID = -7655180560879193585L;

	public String execute() {
		return SUCCESS;
	}

}
