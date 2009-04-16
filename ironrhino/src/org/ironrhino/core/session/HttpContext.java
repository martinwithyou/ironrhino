package org.ironrhino.core.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpContext {

	private HttpServletRequest request;

	private HttpServletResponse response;

	private ServletContext context;

	public HttpContext(HttpServletRequest request,
			HttpServletResponse response, ServletContext context) {
		this.request = request;
		this.response = response;
		this.context = context;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public ServletContext getContext() {
		return context;
	}

	public void setContext(ServletContext context) {
		this.context = context;
	}
}
