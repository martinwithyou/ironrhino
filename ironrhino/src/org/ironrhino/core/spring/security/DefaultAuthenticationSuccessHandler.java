package org.ironrhino.core.spring.security;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Singleton
@Named
public class DefaultAuthenticationSuccessHandler implements
		AuthenticationSuccessHandler {

	public final static String COOKIE_NAME_LOGIN_USER = "U";

	@Value("${authenticationSuccessHandler.usernameInCookie:true}")
	private boolean usernameInCookie;

	@Value("${authenticationSuccessHandler.usernameInCookieMaxAge:31536000}")
	private int usernameInCookieMaxAge;

	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		if (usernameInCookie && request.isRequestedSessionIdFromCookie()) {
			RequestUtils.saveCookie(request, response, COOKIE_NAME_LOGIN_USER,
					authentication.getName(), usernameInCookieMaxAge, true,
					false);
		}
	}

}
