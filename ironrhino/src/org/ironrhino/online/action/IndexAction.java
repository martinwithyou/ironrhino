package org.ironrhino.online.action;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.monitor.Monitor;
import org.ironrhino.online.service.ProductFacade;

@AutoConfig(namespace = "/")
public class IndexAction extends BaseAction {

	private static final long serialVersionUID = -8007101751932155905L;
	
	private transient ProductFacade productFacade;

	public ProductFacade getProductFacade() {
		return productFacade;
	}

	public void setProductFacade(ProductFacade productFacade) {
		this.productFacade = productFacade;
	}

	@Override
	public String execute() {
		Monitor.add("view", "index");
		return SUCCESS;
	}

}
