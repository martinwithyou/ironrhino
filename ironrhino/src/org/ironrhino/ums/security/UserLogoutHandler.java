package org.ironrhino.ums.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.common.util.RequestUtils;
import org.springframework.security.Authentication;
import org.springframework.security.ui.logout.LogoutHandler;

public class UserLogoutHandler implements LogoutHandler {

	@Override
	public void logout(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) {
		RequestUtils.deleteCookie(request, response,
				UserAuthenticationProcessingFilter.ENCRYPT_LOGIN_USER, true);
	}

}
