package org.ironrhino.core.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class HttpWrappedRequest extends HttpServletRequestWrapper {

	private HttpWrappedSession session;

	public HttpWrappedRequest(HttpServletRequest request, HttpContext context,
			HttpSessionManager sessionManager) {
		super(request);
		session = new HttpWrappedSession(context, sessionManager);
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return session;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return session.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return session.isRequestedSessionIdFromURL();
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

}
