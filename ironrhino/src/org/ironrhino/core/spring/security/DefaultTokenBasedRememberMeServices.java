package org.ironrhino.core.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.core.util.RequestUtils;
import org.springframework.security.Authentication;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.StringUtils;

public class DefaultTokenBasedRememberMeServices extends
		TokenBasedRememberMeServices {
	@Override
	protected int calculateLoginLifetime(HttpServletRequest request,
			Authentication authentication) {
		String value = request.getParameter(getParameter());
		if (StringUtils.hasText(value)) {
			try {
				int tokenValiditySeconds = Integer.parseInt(value.trim());
				if (tokenValiditySeconds < 0)
					tokenValiditySeconds = 60 * 60 * 24 * 365 * 5; // 5 years
				return tokenValiditySeconds;
			} catch (Exception e) {
			}
		}
		return getTokenValiditySeconds();
	}

	@Override
	protected boolean rememberMeRequested(HttpServletRequest request,
			String parameter) {
		if (StringUtils.hasText(request.getParameter(parameter)))
			return true;
		return false;
	}

	@Override
	protected void setCookie(String[] tokens, int maxAge,
			HttpServletRequest request, HttpServletResponse response) {
		String cookieValue = encodeCookie(tokens);
		RequestUtils.saveCookie(request, response, getCookieName(),
				cookieValue, maxAge, true);
	}

	@Override
	protected void cancelCookie(HttpServletRequest request,
			HttpServletResponse response) {
		RequestUtils.deleteCookie(request, response, getCookieName(), true);
	}
}
