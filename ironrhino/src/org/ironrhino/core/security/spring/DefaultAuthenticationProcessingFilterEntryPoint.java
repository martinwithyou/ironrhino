package org.ironrhino.core.security.spring;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.springframework.security.util.RedirectUrlBuilder;

public class DefaultAuthenticationProcessingFilterEntryPoint extends
		AuthenticationProcessingFilterEntryPoint {

	public static final String TARGET_URL = "targetUrl";

	private String base;
	private String loginUrl;

	@PostConstruct
	public void init() {
		loginUrl = base + getLoginFormUrl();
	}

	protected String buildRedirectUrlToLoginPage(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException) {

		String targetUrl = null;
		String redirectUrl = null;
		SavedRequest savedRequest = (SavedRequest) request
				.getSession()
				.getAttribute(
						AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
		request.getSession().removeAttribute(
				AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
		if (savedRequest != null)
			targetUrl = savedRequest.getFullRequestUrl();
		if (loginUrl.indexOf("://") < 0)
			throw new IllegalArgumentException("loginForm must be absolute");
		try {
			redirectUrl = loginUrl
					+ (StringUtils.isNotBlank(targetUrl) ? "?" + TARGET_URL
							+ "=" + URLEncoder.encode(targetUrl, "UTF-8") : "");
			if (isForceHttps() && redirectUrl.startsWith("http://")) {
				URL url = new URL(redirectUrl);
				RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
				urlBuilder.setScheme("https");
				urlBuilder.setServerName(url.getHost());
				Integer httpsPort = getPortMapper().lookupHttpsPort(
						url.getPort());
				urlBuilder.setPort(httpsPort);
				urlBuilder.setPathInfo(url.getPath());
				urlBuilder.setQuery(url.getQuery());
				redirectUrl = urlBuilder.getUrl();
			}
		} catch (UnsupportedEncodingException e) {
			redirectUrl = loginUrl;
		} catch (MalformedURLException e2) {
			redirectUrl = loginUrl;
		}
		return redirectUrl;
	}

	public void setBase(String base) {
		this.base = base;

	}

}
