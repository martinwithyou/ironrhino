package org.ironrhino.core.ext.spring.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.Authentication;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.StringUtils;

public class CustomizableTokenBasedRememberMeServices extends
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
}
