package org.ironrhino.online.action;

import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Monitor;
import org.ironrhino.online.service.ProductFacade;

@AutoConfig(namespace = "/")
public class IndexAction extends BaseAction {

	private ProductFacade productFacade;

	public ProductFacade getProductFacade() {
		return productFacade;
	}

	public void setProductFacade(ProductFacade productFacade) {
		this.productFacade = productFacade;
	}

	public String execute() {
		Monitor.add(new Key("首页", "访问"));
		return SUCCESS;
	}

}
