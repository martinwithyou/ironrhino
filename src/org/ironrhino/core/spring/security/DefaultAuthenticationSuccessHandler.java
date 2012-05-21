package org.ironrhino.core.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.core.util.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class DefaultAuthenticationSuccessHandler implements
		AuthenticationSuccessHandler {

	public final static String COOKIE_NAME_LOGIN_USER = "U";

	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		String username = authentication.getName();
		if (request.isRequestedSessionIdFromCookie())
			RequestUtils.saveCookie(request, response, COOKIE_NAME_LOGIN_USER,
					username, 365 * 24 * 3600, true, false);
	}

}
