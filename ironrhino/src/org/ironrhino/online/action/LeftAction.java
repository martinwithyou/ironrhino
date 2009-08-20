package org.ironrhino.online.action;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.pms.support.CategoryTreeControl;

@AutoConfig(namespace = "/")
public class LeftAction extends BaseAction {

	private static final long serialVersionUID = 6640138055634199058L;

	private transient CategoryTreeControl categoryTreeControl;

	public CategoryTreeControl getCategoryTreeControl() {
		return categoryTreeControl;
	}

	public void setCategoryTreeControl(CategoryTreeControl categoryTreeControl) {
		this.categoryTreeControl = categoryTreeControl;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

}
