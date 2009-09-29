package com.ironrhino.online.action;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.ironrhino.pms.support.CategoryTreeControl;

@AutoConfig(namespace = "/")
public class LeftAction extends BaseAction {

	private static final long serialVersionUID = 6640138055634199058L;

	@Autowired
	private transient CategoryTreeControl categoryTreeControl;

	public CategoryTreeControl getCategoryTreeControl() {
		return categoryTreeControl;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

}
