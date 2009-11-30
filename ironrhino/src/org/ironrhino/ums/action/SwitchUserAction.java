package org.ironrhino.ums.action;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

@AutoConfig(namespace = "/")
public class SwitchUserAction extends BaseAction {

	private static final long serialVersionUID = -7655180560879193585L;

	@Override
	public String execute() {
		return SUCCESS;
	}

}
