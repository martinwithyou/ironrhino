package org.ironrhino.security.socialauth.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.util.HttpClientUtils;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.security.socialauth.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OAuth20Provider extends AbstractAuthProvider {

	protected static Logger logger = LoggerFactory
			.getLogger(OAuth20Provider.class);

	protected static ObjectMapper mapper = new ObjectMapper();

	protected static DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

	public abstract String getAuthorizeUrl();

	public abstract String getAccessTokenEndpoint();

	public String getScope() {
		return null;
	}

	public String getState() {
		return null;
	}

	public boolean isUseAuthorizationHeader() {
		return false;
	}

	public String getClientId() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".clientId");
	}

	public String getClientSecret() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".clientSecret");
	}

	public boolean isDiscoverable() {
		return false;
	}

	@PostConstruct
	public void afterPropertiesSet() {
		String key = getClientId();
		String secret = getClientSecret();
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(secret)) {
			forceDisabled = true;
			logger.warn("key or secret is empty, disabled " + getName());
		}
	}

	public String getLoginRedirectURL(HttpServletRequest request,
			String returnToURL) throws Exception {
		OAuth20AccessToken accessToken = restoreToken(request);
		if (accessToken != null) {
			if (accessToken.isExpired()) {
				try {
					accessToken = refreshToken(accessToken);
					saveToken(request, accessToken);
					return returnToURL;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				return returnToURL;
			}
		}
		StringBuilder sb = new StringBuilder(getAuthorizeUrl()).append('?')
				.append("client_id").append('=').append(getClientId())
				.append('&').append("redirect_uri").append('=')
				.append(URLEncoder.encode(returnToURL, "UTF-8"));
		sb.append("&response_type=code");
		String scope = getScope();
		if (StringUtils.isNotBlank(scope))
			try {
				sb.append('&').append("scope").append('=')
						.append(URLEncoder.encode(scope, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		String state = getState();
		if (StringUtils.isNotBlank(state))
			sb.append('&').append("state").append('=').append(state);
		return sb.toString();
	}

	public Profile getProfile(HttpServletRequest request) throws Exception {

		OAuth20AccessToken accessToken = restoreToken(request);
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
			if (StringUtils.isNotBlank(request.getParameter("error")))
				return null;
			Map<String, String> params = new HashMap<String, String>();
			params.put("code", request.getParameter("code"));
			params.put("client_id", getClientId());
			params.put("client_secret", getClientSecret());
			params.put("redirect_uri", RequestUtils.getBaseUrl(request)
					+ RequestUtils.getRequestUri(request));
			params.put("grant_type", "authorization_code");
			String content = HttpClientUtils.postResponseText(
					getAccessTokenEndpoint(), params);
			accessToken = JsonUtils.fromJson(content, OAuth20AccessToken.class);
			saveToken(request, accessToken);
		}
		return doGetProfile(accessToken.getAccess_token());
	}

	public String invoke(HttpServletRequest request, String protectedURL)
			throws Exception {
		OAuth20AccessToken accessToken = restoreToken(request);
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
			map.put("oauth_token", accessToken);
		else
			map.put("Authorization", "OAuth " + accessToken);
		return invoke(protectedURL, isUseAuthorizationHeader() ? null : map,
				isUseAuthorizationHeader() ? map : null);
	}

	protected String invoke(String protectedURL, Map<String, String> params,
			Map<String, String> headers) {
		return HttpClientUtils.getResponseText(protectedURL, params, headers);
	}

	protected abstract Profile doGetProfile(String accessToken)
			throws Exception;

	public OAuth20AccessToken refreshToken(OAuth20AccessToken accessToken)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", getClientId());
		params.put("client_secret", getClientSecret());
		params.put("refresh_token", accessToken.getRefresh_token());
		params.put("grant_type", "refresh_token");
		String content = HttpClientUtils.postResponseText(
				getAccessTokenEndpoint(), params);
		return JsonUtils.fromJson(content, OAuth20AccessToken.class);
	}

	protected void saveToken(HttpServletRequest request,
			OAuth20AccessToken token) {
		token.setCreate_time((int) System.currentTimeMillis() / 1000);
		request.getSession().setAttribute(getName() + "_token",
				JsonUtils.toJson(token));
	}

	protected OAuth20AccessToken restoreToken(HttpServletRequest request)
			throws Exception {
		String key = getName() + "_token";
		String json = (String) request.getSession().getAttribute(key);
		if (StringUtils.isBlank(json))
			return null;
		return JsonUtils.fromJson(json, OAuth20AccessToken.class);
	}

	protected String generateId(String uid) {
		return "(" + getName() + ")" + uid;
	}

	public static class OAuth20AccessToken {

		private String access_token;
		private String token_type;
		private int expires_in;
		private int create_time;
		private String refresh_token;

		public String getAccess_token() {
			return access_token;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public String getToken_type() {
			return token_type;
		}

		public void setToken_type(String token_type) {
			this.token_type = token_type;
		}

		public int getExpires_in() {
			return expires_in;
		}

		public void setExpires_in(int expires_in) {
			this.expires_in = expires_in;
		}

		public String getRefresh_token() {
			return refresh_token;
		}

		public void setRefresh_token(String refresh_token) {
			this.refresh_token = refresh_token;
		}

		public int getCreate_time() {
			return create_time;
		}

		public void setCreate_time(int create_time) {
			this.create_time = create_time;
		}

		@NotInJson
		public boolean isExpired() {
			if (expires_in <= 0 || create_time <= 0)
				return false;
			int offset = 60;
			int current = (int) System.currentTimeMillis() / 1000;
			return current - create_time > expires_in - offset;
		}

	}

}
