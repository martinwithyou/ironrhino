package org.ironrhino.core.struts;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;

public class DirectPageResult extends AutoConfigResult {

	@Override
	protected String conditionalParse(String param, ActionInvocation invocation) {
		HttpServletRequest request = ServletActionContext.getRequest();
		String uri = AbstractActionMapper.getUri(request);
		return getTemplateLocation(uri);
	}

}
