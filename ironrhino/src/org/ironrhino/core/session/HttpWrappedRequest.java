package org.ironrhino.core.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.util.RequestUtils;

public class HttpWrappedRequest extends HttpServletRequestWrapper {

	private HttpWrappedSession session;

	private boolean requestedSessionIdFromCookie;

	private boolean requestedSessionIdFromURL;

	public HttpWrappedRequest(HttpServletRequest request, HttpContext context,
			HttpSessionManager sessionManager) {
		super(request);
		String sessionId = RequestUtils.getCookieValue(context.getRequest(),
				Constants.COOKIE_NAME_SESSION_ID);
		if (StringUtils.isNotBlank(sessionId)) {
			requestedSessionIdFromCookie = true;
		} else {
			sessionId = request.getParameter(Constants.COOKIE_NAME_SESSION_ID);
			if (StringUtils.isBlank(sessionId)) {
				String requestURL = request.getRequestURL().toString();
				if (requestURL.indexOf(';') > -1) {
					requestURL = requestURL
							.substring(requestURL.indexOf(';') + 1);
					String[] array = requestURL.split(";");
					for (String pair : array) {
						if (pair.indexOf('=') > 0) {
							String k = pair.substring(0, pair.indexOf('='));
							if (k.equals(Constants.COOKIE_NAME_SESSION_ID)) {
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
		session = new HttpWrappedSession(context, sessionManager, sessionId);
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
		return requestedSessionIdFromCookie;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return requestedSessionIdFromURL;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return requestedSessionIdFromURL;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

}
