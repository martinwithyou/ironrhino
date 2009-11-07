package org.ironrhino.core.security.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;

public class BaseAuthenticationProcessingFilter extends
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

}
