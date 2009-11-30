package com.ironrhino.online.action;

import javax.inject.Inject;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

import com.ironrhino.pms.support.CategoryTreeControl;

@AutoConfig(namespace = "/")
public class LeftAction extends BaseAction {

	private static final long serialVersionUID = 6640138055634199058L;

	@Inject
	private transient CategoryTreeControl categoryTreeControl;

	public CategoryTreeControl getCategoryTreeControl() {
		return categoryTreeControl;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

}
