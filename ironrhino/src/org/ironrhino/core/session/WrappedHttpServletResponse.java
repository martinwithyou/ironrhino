package org.ironrhino.core.session;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WrappedHttpServletResponse extends HttpServletResponseWrapper {

	private WrappedHttpSession session;

	public WrappedHttpServletResponse(HttpServletResponse response,
			WrappedHttpSession session) {
		super(response);
		this.session = session;
	}

	@Override
	public String encodeURL(String url) {
		return session.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return session.encodeRedirectURL(url);
	}

}
