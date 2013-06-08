package org.ironrhino.core.spring.security;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Singleton
@Named
public class DefaultAuthenticationFailureHandler implements
		AuthenticationFailureHandler {

	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		request.getSession().removeAttribute(
				WebAttributes.AUTHENTICATION_EXCEPTION);
	}

}
