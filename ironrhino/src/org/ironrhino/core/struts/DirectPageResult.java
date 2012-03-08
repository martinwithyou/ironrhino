package org.ironrhino.core.struts;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;

public class DirectPageResult extends AutoConfigResult {

	private static final long serialVersionUID = 3152452358832384680L;

	@Override
	protected String conditionalParse(String param, ActionInvocation invocation) {
		HttpServletRequest request = ServletActionContext.getRequest();
		String uri = AbstractActionMapper.getUri(request);
		return getTemplateLocation(uri);
	}

}
