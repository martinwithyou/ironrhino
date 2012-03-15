package org.ironrhino.security.oauth.client.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.util.HttpClientUtils;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.OAuth2Token;
import org.ironrhino.security.oauth.client.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OAuth2Provider extends AbstractOAuthProvider {

	protected static Logger logger = LoggerFactory
			.getLogger(OAuth2Provider.class);

	public abstract String getAuthorizeUrl();

	public abstract String getAccessTokenEndpoint();

	public String getScope() {
		return null;
	}

	public String getClientId() {
		return settingControl
				.getStringValue("oauth." + getName() + ".clientId");
	}

	public String getClientSecret() {
		return settingControl.getStringValue("oauth." + getName()
				+ ".clientSecret");
	}

	public boolean isEnabled() {
		return super.isEnabled() && StringUtils.isNotBlank(getClientId());
	}

	protected String getAuthorizationHeaderName() {
		return "Authorization";
	}

	protected String getAuthorizationHeaderType() {
		return "Bearer";
	}

	protected String getAccessTokenParameterName() {
		return "oauth_token";
	}

	@PostConstruct
	public void afterPropertiesSet() {
		String clientId = getClientId();
		String clientSecret = getClientSecret();
		if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
			logger.warn(getName() + " clientId or clientSecret is empty");
		}
	}

	public String getAuthRedirectURL(HttpServletRequest request,
			String targetUrl) throws Exception {
		OAuth2Token accessToken = restoreToken(request);
		if (accessToken != null) {
			if (accessToken.isExpired()) {
				try {
					accessToken = refreshToken(accessToken);
					saveToken(request, accessToken);
					return targetUrl;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				return targetUrl;
			}
		}
		String state = null;
		if (targetUrl.indexOf('?') > 0) {
			state = targetUrl.substring(targetUrl.lastIndexOf('=') + 1);
			targetUrl = targetUrl.substring(0, targetUrl.indexOf('?'));
		}
		StringBuilder sb = new StringBuilder(getAuthorizeUrl()).append('?')
				.append("client_id").append('=').append(getClientId())
				.append('&').append("redirect_uri").append('=')
				.append(URLEncoder.encode(targetUrl, "UTF-8"));
		sb.append("&response_type=code");
		String scope = getScope();
		if (StringUtils.isNotBlank(scope))
			try {
				sb.append('&').append("scope").append('=')
						.append(URLEncoder.encode(scope, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		if (StringUtils.isNotBlank(state))
			sb.append('&').append("state").append('=').append(state);
		return sb.toString();
	}

	public Profile getProfile(HttpServletRequest request) throws Exception {

		OAuth2Token accessToken = restoreToken(request);
		if (accessToken != null) {
			if (accessToken.isExpired()) {
				try {
					accessToken = refreshToken(accessToken);
					saveToken(request, accessToken);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		} else {
			String error = request.getParameter("error");
			if (StringUtils.isNotBlank(error))
				return null;
			Map<String, String> params = new HashMap<String, String>();
			params.put("code", request.getParameter("code"));
			params.put("client_id", getClientId());
			params.put("client_secret", getClientSecret());
			params.put("redirect_uri", request.getRequestURL().toString());
			params.put("grant_type", "authorization_code");
			String content = HttpClientUtils.postResponseText(
					getAccessTokenEndpoint(), params);
			if (JsonUtils.isValidJson(content)) {
				Map<String, String> map = JsonUtils.fromJson(content,
						new TypeReference<Map<String, String>>() {
						});
				accessToken = new OAuth2Token();
				accessToken.setAccess_token(map.get("access_token"));
				accessToken.setToken_type(map.get("token_type"));
				accessToken.setRefresh_token(map.get("refresh_token"));
				if (map.get("expires_in") != null)
					accessToken.setExpires_in(Integer.valueOf(map
							.get("expires_in")));
			} else {
				accessToken = new OAuth2Token();
				String[] arr1 = content.split("&");
				for (String s : arr1) {
					String[] arr2 = s.split("=", 2);
					if (arr2.length > 1 && arr2[0].equals("access_token"))
						accessToken.setAccess_token(arr2[1]);
					else if (arr2.length > 1 && arr2[0].equals("token_type"))
						accessToken.setToken_type(arr2[1]);
				}
			}
			saveToken(request, accessToken);
		}
		String content = invoke(accessToken.getAccess_token(), getProfileUrl());
		Profile p = getProfileFromContent(content);
		postProcessProfile(p, accessToken.getAccess_token());
		return p;
	}

	protected void postProcessProfile(Profile p, String accessToken)
			throws Exception {
	}

	public String invoke(HttpServletRequest request, String protectedURL)
			throws Exception {
		OAuth2Token accessToken = restoreToken(request);
		if (accessToken == null)
			throw new IllegalAccessException("must already authorized");
		if (accessToken.isExpired()) {
			try {
				accessToken = refreshToken(accessToken);
				saveToken(request, accessToken);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return invoke(accessToken.getAccess_token(), protectedURL);

	}

	public String invoke(String accessToken, String protectedURL)
			throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		if (!isUseAuthorizationHeader())
			map.put(getAccessTokenParameterName(), accessToken);
		else
			map.put(getAuthorizationHeaderName(), getAuthorizationHeaderType()
					+ " " + accessToken);
		return invoke(protectedURL, isUseAuthorizationHeader() ? null : map,
				isUseAuthorizationHeader() ? map : null);
	}

	protected String invoke(String protectedURL, Map<String, String> params,
			Map<String, String> headers) {
		return HttpClientUtils.getResponseText(protectedURL, params, headers);
	}

	public OAuth2Token refreshToken(OAuth2Token accessToken) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", getClientId());
		params.put("client_secret", getClientSecret());
		params.put("refresh_token", accessToken.getRefresh_token());
		params.put("grant_type", "refresh_token");
		String content = HttpClientUtils.postResponseText(
				getAccessTokenEndpoint(), params);
		return JsonUtils.fromJson(content, OAuth2Token.class);
	}

	protected void saveToken(HttpServletRequest request, OAuth2Token token) {
		token.setCreate_time(System.currentTimeMillis());
		request.getSession().setAttribute(getName() + "_token",
				JsonUtils.toJson(token));
	}

	protected OAuth2Token restoreToken(HttpServletRequest request)
			throws Exception {
		String key = getName() + "_token";
		String json = (String) request.getSession().getAttribute(key);
		if (StringUtils.isBlank(json))
			return null;
		return JsonUtils.fromJson(json, OAuth2Token.class);
	}

}
