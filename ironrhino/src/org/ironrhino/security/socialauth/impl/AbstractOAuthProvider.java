package org.ironrhino.security.socialauth.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.socialauth.Profile;

public abstract class AbstractOAuthProvider extends AbstractAuthProvider {

	@Inject
	protected SettingControl settingControl;

	protected String consumerKey;
	protected String consumerSecret;
	protected String requestTokenUrl;
	protected String authorizeUrl;
	protected String accessTokenUrl;

	protected OAuthServiceProvider serviceProvider;

	@PostConstruct
	public void afterPropertiesSet() {
		try {
			for (Field f : AbstractOAuthProvider.class.getDeclaredFields()) {
				int mod = f.getModifiers();
				if (f.getType().equals(String.class)
						&& Modifier.isProtected(mod) && !Modifier.isStatic(mod)) {
					if (f.get(this) == null) {
						f.setAccessible(true);
						f.set(this, settingControl.getStringValue("oauth."
								+ getName() + "." + f.getName()));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		serviceProvider = new OAuthServiceProvider(requestTokenUrl,
				authorizeUrl, accessTokenUrl);
	}

	public String getLoginRedirectURL(HttpServletRequest request,
			String returnToURL) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(returnToURL, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new HttpClient4());
		client.getRequestToken(accessor);
		Map<String, String> token = new HashMap<String, String>();
		token.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
		token.put(OAuth.OAUTH_TOKEN_SECRET, accessor.tokenSecret);
		saveToken(request, token);
		token.put(OAuth.OAUTH_CALLBACK, returnToURL);
		OAuthMessage response = client.invoke(accessor, "GET",
				serviceProvider.userAuthorizationURL, token.entrySet());
		return response.URL;
	}

	public Profile getProfile(HttpServletRequest request) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new HttpClient4());
		Map<String, String> token = restoreToken(request);
		accessor.requestToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		client.getAccessToken(accessor, "GET", token.entrySet());
		token.put(OAuth.OAUTH_TOKEN, accessor.accessToken);
		token.put(OAuth.OAUTH_TOKEN_SECRET, accessor.tokenSecret);
		saveToken(request, token);
		return doGetProfile(token);
	}

	protected abstract Profile doGetProfile(Map<String, String> token)
			throws Exception;

	protected OAuthMessage sendRequest(HttpServletRequest request, Map map,
			String url) throws Exception {
		Map<String, String> token = restoreToken(request);
		map.putAll(token);
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		OAuthClient client = new OAuthClient(new HttpClient4());
		return client.invoke(accessor, "GET", url, map.entrySet());
	}

	protected void saveToken(HttpServletRequest request,
			Map<String, String> token) {
		request.getSession().setAttribute(getName() + "_token",
				JsonUtils.toJson(token));
	}

	protected Map<String, String> restoreToken(HttpServletRequest request)
			throws Exception {
		String key = getName() + "_token";
		String json = (String) request.getSession().getAttribute(key);
		return JsonUtils.fromJson(json,
				new TypeReference<Map<String, String>>() {
				});
	}

}
