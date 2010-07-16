package org.ironrhino.core.spring.security;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.RedirectUrlBuilder;

public class DefaultLoginUrlAuthenticationEntryPoint extends
		LoginUrlAuthenticationEntryPoint {

	@Value("${sso.server.base:}")
	private String ssoServerBase;

	private String loginUrl;

	public void setSsoServerBase(String ssoServerBase) {
		this.ssoServerBase = ssoServerBase;
	}

	@PostConstruct
	public void init() {
		if (StringUtils.isNotBlank(ssoServerBase))
			loginUrl = ssoServerBase + getLoginFormUrl();
	}

	@Override
	protected String buildRedirectUrlToLoginPage(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException) {
		String targetUrl = null;
		String redirectUrl = null;
		SavedRequest savedRequest = (SavedRequest) request.getSession()
				.getAttribute(WebAttributes.SAVED_REQUEST);
		request.getSession().removeAttribute(WebAttributes.SAVED_REQUEST);
		if (savedRequest != null)
			targetUrl = savedRequest.getRedirectUrl();
		if (loginUrl == null) {
			URL url = null;
			try {
				url = new URL(request.getRequestURL().toString());
			} catch (MalformedURLException e) {

			}
			loginUrl = request.getContextPath() + loginUrl;
			RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
			String scheme = request.getScheme();
			int serverPort = getPortResolver().getServerPort(request);
			urlBuilder.setScheme(scheme);
			urlBuilder.setServerName(url.getHost());
			urlBuilder.setPort(serverPort);
			urlBuilder.setContextPath(request.getContextPath());
			urlBuilder.setPathInfo(getLoginFormUrl());
			if (isForceHttps() && "http".equals(scheme)) {
				Integer httpsPort = getPortMapper().lookupHttpsPort(serverPort);
				urlBuilder.setScheme("https");
				urlBuilder.setPort(httpsPort);
			}
			loginUrl = urlBuilder.getUrl();
		}
		try {
			redirectUrl = loginUrl
					+ (StringUtils.isNotBlank(targetUrl) ? "?"
							+ DefaultUsernamePasswordAuthenticationFilter.TARGET_URL
							+ "=" + URLEncoder.encode(targetUrl, "UTF-8")
							: "");
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

}
