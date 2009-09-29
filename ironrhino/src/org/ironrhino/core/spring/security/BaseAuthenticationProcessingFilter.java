package org.ironrhino.core.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.core.security.util.PGP;
import org.ironrhino.core.session.Constants;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;

public class BaseAuthenticationProcessingFilter extends
		AuthenticationProcessingFilter {

	@Autowired
	private AuthenticationProcessingFilterEntryPoint entryPoint;

	@Override
	protected void onSuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException {
		String username = authResult.getName();
		RequestUtils.saveCookie(request, response,
				Constants.COOKIE_NAME_LOGIN_USER, username, 365 * 24 * 3600,
				true);
		RequestUtils.saveCookie(request, response,
				Constants.COOKIE_NAME_ENCRYPT_LOGIN_USER,
				PGP.encrypt(username), true);
	}

	protected String determineTargetUrl(HttpServletRequest request) {
		return entryPoint.getLoginFormUrl();
	}

	protected void sendRedirect(HttpServletRequest request,
			HttpServletResponse response, String url) throws IOException {
		try {
			request.getRequestDispatcher(url).forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

}
