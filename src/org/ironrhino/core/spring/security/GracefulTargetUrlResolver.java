package org.ironrhino.core.spring.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.Authentication;
import org.springframework.security.ui.TargetUrlResolver;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

public class GracefulTargetUrlResolver implements TargetUrlResolver {

	public String determineTargetUrl(SavedRequest arg0,
			HttpServletRequest request, Authentication arg2) {

		String targetUrl = null;
		if (!StringUtils.hasLength(targetUrl)
				&& StringUtils.hasLength(request.getHeader("Referer")))
			targetUrl = request.getHeader("Referer");

		if (StringUtils.hasLength(request.getParameter("targetUrl")))
			targetUrl = request.getParameter("targetUrl");

		if (!StringUtils.hasLength(targetUrl))
			targetUrl = request.getContextPath();

		return targetUrl;
	}

}
