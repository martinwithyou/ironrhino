package org.ironrhino.common.action;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;

@AutoConfig
public class ControlPanelAction extends BaseAction {

	private static final long serialVersionUID = -8640589864035822713L;

	@Override
	public String execute() {
		return SUCCESS;
	}

}
