package org.ironrhino.core.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.util.RequestUtils;

public class HttpRequest extends HttpServletRequestWrapper {

	private Session session;

	private boolean requestedSessionIdFromCookie;

	private boolean requestedSessionIdFromURL;

	public HttpRequest(HttpServletRequest request, HttpContext context,
			SessionManager sessionManager) {
		super(request);
		String sessionId = RequestUtils.getCookieValue(context.getRequest(),
				Session.SESSION_ID);
		if (StringUtils.isNotBlank(sessionId)) {
			requestedSessionIdFromCookie = true;
		} else {
			sessionId = request.getParameter(Session.SESSION_ID);
			if (StringUtils.isBlank(sessionId)) {
				String requestURL = request.getRequestURL().toString();
				if (requestURL.indexOf(';') > -1) {
					requestURL = requestURL
							.substring(requestURL.indexOf(';') + 1);
					String[] array = requestURL.split(";");
					for (String pair : array) {
						if (pair.indexOf('=') > 0) {
							String k = pair.substring(0, pair.indexOf('='));
							if (k.equals(Session.SESSION_ID)) {
								sessionId = pair
										.substring(pair.indexOf('=') + 1);
								break;
							}
						}
					}
				}
			}
			if (StringUtils.isNotBlank(sessionId)) {
				requestedSessionIdFromURL = true;
			}
		}
		session = new Session(context, sessionManager, sessionId);
	}

	public HttpSession getSession() {
		return session;
	}

	public HttpSession getSession(boolean create) {
		return session;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return requestedSessionIdFromCookie;
	}

	public boolean isRequestedSessionIdFromURL() {
		return requestedSessionIdFromURL;
	}

	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return requestedSessionIdFromURL;
	}

}
