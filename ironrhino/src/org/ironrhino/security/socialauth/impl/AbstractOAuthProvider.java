package org.ironrhino.security.socialauth.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.socialauth.Profile;

public abstract class AbstractOAuthProvider extends AbstractAuthProvider {

	protected OAuthServiceProvider serviceProvider;

	public String getRequestTokenUrl() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".requestTokenUrl");
	}

	public String getAuthorizeUrl() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".authorizeUrl");
	}

	public String getAccessTokenUrl() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".accessTokenUrl");
	}

	public String getConsumerKey() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".consumerKey");
	}

	public String getConsumerSecret() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".consumerSecret");
	}

	protected static ObjectMapper mapper = new ObjectMapper();

	protected static DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

	public boolean isDiscoverable() {
		return false;
	}

	@PostConstruct
	public void init() {
		String key = getConsumerKey();
		String secret = getConsumerSecret();
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(secret))
			forceDisabled = true;
		else
			serviceProvider = new OAuthServiceProvider(getRequestTokenUrl(),
					getAuthorizeUrl(), getAccessTokenUrl());
	}

	public String getLoginRedirectURL(HttpServletRequest request,
			String returnToURL) throws Exception {
		Map<String, String> accessToken = restoreToken(request, "access");
		if (accessToken != null)
			return returnToURL;
		OAuthConsumer consumer = new OAuthConsumer(returnToURL,
				getConsumerKey(), getConsumerSecret(), serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		Map<String, String> map = new HashMap<String, String>(4);
		map.put(OAuth.OAUTH_CALLBACK, returnToURL);
		client.getRequestToken(accessor, null, map.entrySet());
		map.clear();
		map.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
		map.put(OAuth.OAUTH_TOKEN_SECRET, accessor.tokenSecret);
		saveToken(request, map, "request");
		return new StringBuilder(getAuthorizeUrl()).append("?oauth_token=")
				.append(accessor.requestToken).append("&oauth_callback=")
				.append(URLEncoder.encode(returnToURL, "UTF-8")).toString();
	}

	public Profile getProfile(HttpServletRequest request) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(null, getConsumerKey(),
				getConsumerSecret(), serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		Map<String, String> accessToken = restoreToken(request, "access");
		if (accessToken == null) {
			Map<String, String> requestToken = restoreToken(request, "request");
			removeToken(request, "request");
			accessor.requestToken = requestToken.get(OAuth.OAUTH_TOKEN);
			accessor.tokenSecret = requestToken.get(OAuth.OAUTH_TOKEN_SECRET);
			requestToken.clear();
			String oauth_verifier = request.getParameter(OAuth.OAUTH_VERIFIER);
			if (oauth_verifier != null)
				requestToken.put(OAuth.OAUTH_VERIFIER, oauth_verifier);
			client.getAccessToken(accessor, "GET", requestToken.entrySet());
			requestToken.clear();
			accessToken = requestToken;
			accessToken.put(OAuth.OAUTH_TOKEN, accessor.accessToken);
			accessToken.put(OAuth.OAUTH_TOKEN_SECRET, accessor.tokenSecret);
			saveToken(request, accessToken, "access");
		} else {
			accessor.accessToken = accessToken.get(OAuth.OAUTH_TOKEN);
			accessor.tokenSecret = accessToken.get(OAuth.OAUTH_TOKEN_SECRET);
		}
		return doGetProfile(client, accessor);
	}

	protected abstract Profile doGetProfile(OAuthClient client,
			OAuthAccessor accessor) throws Exception;

	protected OAuthMessage sendRequest(HttpServletRequest request, Map map,
			String url) throws Exception {
		Map<String, String> token = restoreToken(request, "access");
		OAuthConsumer consumer = new OAuthConsumer(null, getConsumerKey(),
				getConsumerSecret(), serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		return client.invoke(accessor, "GET", url, map != null ? map.entrySet()
				: null);
	}

	protected void saveToken(HttpServletRequest request,
			Map<String, String> token, String type) {
		request.getSession().setAttribute(tokenSessionKey(type),
				JsonUtils.toJson(token));
	}

	protected void removeToken(HttpServletRequest request, String type) {
		request.getSession().removeAttribute(tokenSessionKey(type));
	}

	protected Map<String, String> restoreToken(HttpServletRequest request,
			String type) throws Exception {
		String json = (String) request.getSession().getAttribute(
				tokenSessionKey(type));
		if (StringUtils.isBlank(json))
			return null;
		return JsonUtils.fromJson(json,
				new TypeReference<Map<String, String>>() {
				});
	}

	private String tokenSessionKey(String type) {
		return new StringBuffer(getName()).append('_').append(type)
				.append("_token").toString();
	}

	protected String generateId(String uid) {
		return "(" + getName() + ")" + uid;
	}

}
