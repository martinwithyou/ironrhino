package org.ironrhino.core.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.springframework.security.util.SessionUtils;

public class DefaultuthenticationProcessingFilter extends
		AuthenticationProcessingFilter {

	@Autowired
	private AuthenticationProcessingFilterEntryPoint entryPoint;

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

	public void successfulAuthenticationWithoutRedirect(
			HttpServletRequest request, HttpServletResponse response,
			Authentication authResult) throws IOException, ServletException {
		SecurityContextHolder.getContext().setAuthentication(authResult);
		if (isInvalidateSessionOnSuccessfulAuthentication()) {
			SessionUtils.startNewSessionIfRequired(request,
					isMigrateInvalidatedSessionAttributes(),
					getSessionRegistry());
		}
		onSuccessfulAuthentication(request, response, authResult);
		getRememberMeServices().loginSuccess(request, response, authResult);
		// Fire event
		if (this.eventPublisher != null) {
			eventPublisher
					.publishEvent(new InteractiveAuthenticationSuccessEvent(
							authResult, this.getClass()));
		}
	}

	public void unsuccessfulAuthenticationWithoutRedirect(
			HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException,
			ServletException {
		SecurityContextHolder.getContext().setAuthentication(null);
		onUnsuccessfulAuthentication(request, response, failed);
		getRememberMeServices().loginFail(request, response);
	}

}
