package org.ironrhino.core.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class HttpRequest extends HttpServletRequestWrapper {

	private Session session = null;

	public HttpRequest(HttpServletRequest request, HttpContext context,
			SessionManager sessionManager) {
		super(request);
		session = new Session(context, sessionManager);
	}

	public HttpSession getSession() {
		return this.session;
	}

	public HttpSession getSession(boolean create) {
		return session;
	}
}
