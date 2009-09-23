package org.ironrhino.online.action;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.pms.support.CategoryTreeControl;
import org.springframework.beans.factory.annotation.Autowired;

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
