package com.ironrhino.online.action;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.stat.StatLog;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.ironrhino.online.service.ProductFacade;

@AutoConfig(namespace = "/")
public class IndexAction extends BaseAction {

	private static final long serialVersionUID = -8007101751932155905L;

	@Autowired
	private transient ProductFacade productFacade;

	public ProductFacade getProductFacade() {
		return productFacade;
	}

	@Override
	public String execute() {
		StatLog.add("view", "index");
		return SUCCESS;
	}

}
