package org.ironrhino.security.socialauth.impl;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import net.oauth.OAuth;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.socialauth.Profile;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthConstants;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public abstract class ScribeOAuthProvider extends AbstractAuthProvider {

	protected static ObjectMapper mapper = new ObjectMapper();

	protected static DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

	protected OAuthService oauthService;

	public abstract String getRequestTokenUrl();

	public abstract String getAuthorizeUrl();

	public abstract String getAccessTokenUrl();

	public String getConsumerKey() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".consumerKey");
	}

	public String getConsumerSecret() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".consumerSecret");
	}

	public boolean isDiscoverable() {
		return false;
	}

	class DefaultApi10aClass extends DefaultApi10a {

		public DefaultApi10aClass() {

		}

		@Override
		public String getAccessTokenEndpoint() {
			return getAccessTokenUrl();
		}

		@Override
		public String getRequestTokenEndpoint() {
			return getRequestTokenUrl();
		}

		@Override
		public Verb getAccessTokenVerb() {
			return Verb.GET;
		}

		@Override
		public Verb getRequestTokenVerb() {
			return Verb.GET;
		}

		@Override
		public String getAuthorizationUrl(Token requestToken) {
			return new StringBuilder(getAuthorizeUrl()).append('?')
					.append(OAuthConstants.TOKEN).append('=')
					.append(requestToken.getToken()).toString();
		}
	}

	public DefaultApi10a getApi() {
		return new DefaultApi10aClass();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		String key = getConsumerKey();
		String secret = getConsumerSecret();
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(secret)) {
			forceDisabled = true;
			logger.warn("key or secret is empty, disabled " + getName());
		} else {
			oauthService = new ServiceBuilder().provider(getApi()).apiKey(key)
					.apiSecret(secret).build();
		}
	}

	public String getLoginRedirectURL(HttpServletRequest request,
			String returnToURL) throws Exception {
		Token requestToken = oauthService.getRequestToken();
		saveToken(request, requestToken);
		return new StringBuilder(getAuthorizeUrl()).append('?')
				.append(OAuthConstants.TOKEN).append('=')
				.append(requestToken.getToken()).append('&')
				.append(OAuthConstants.CALLBACK).append("=")
				.append(URLEncoder.encode(returnToURL, "UTF-8")).toString();
	}

	public Profile getProfile(HttpServletRequest request) throws Exception {
		Token requestToken = restoreToken(request);
		String oauth_verifier = request.getParameter(OAuth.OAUTH_VERIFIER);
		Token accessToken = oauthService.getAccessToken(requestToken,
				new Verifier(oauth_verifier));
		saveToken(request, accessToken);
		return doGetProfile(accessToken);
	}

	protected abstract Profile doGetProfile(Token accessToken) throws Exception;

	protected void saveToken(HttpServletRequest request, Token token) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("token", token.getToken());
		map.put("secret", token.getSecret());
		request.getSession().setAttribute(getName() + "_token",
				JsonUtils.toJson(map));
	}

	protected Token restoreToken(HttpServletRequest request) throws Exception {
		String key = getName() + "_token";
		String json = (String) request.getSession().getAttribute(key);
		Map<String, String> map = JsonUtils.fromJson(json,
				new TypeReference<Map<String, String>>() {
				});
		return new Token(map.get("token"), map.get("secret"));
	}

	protected String generateId(String uid) {
		return "(" + getName() + ")" + uid;
	}

	public static void main(String[] args) throws Exception {
		System.out
				.println(URLDecoder
						.decode("%E9%94%99%E8%AF%AF%3A%E7%AD%BE%E5%90%8D%E5%80%BC%E4%B8%8D%E5%90%88%E6%B3%95%21",
								"UTF-8"));
	}

}
