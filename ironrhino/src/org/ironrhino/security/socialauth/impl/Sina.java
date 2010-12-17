package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("sina")
@Singleton
public class Sina extends AbstractOAuthProvider {

	@Value("${sina.requestTokenUrl:http://api.t.sina.com.cn/oauth/request_token}")
	private String requestTokenUrl;

	@Value("${sina.authorizeUrl:http://api.t.sina.com.cn/oauth/authorize}")
	private String authorizeUrl;

	@Value("${sina.accessTokenUrl:http://api.t.sina.com.cn/oauth/access_token}")
	private String accessTokenUrl;

	public String getRequestTokenUrl() {
		return requestTokenUrl;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	@Override
	protected Profile doGetProfile(OAuthClient client, OAuthAccessor accessor)
			throws Exception {
		OAuthMessage message = client.invoke(accessor,
				"http://api.t.sina.com.cn/account/verify_credentials.json",
				null);
		String json = message.readBodyAsString();
		JsonNode data = mapper.readValue(json, JsonNode.class);
		String uid = String.valueOf(data.get("id").getLongValue());
		String name = data.get("name").getTextValue();
		String displayName = data.get("screen_name").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setName(name);
		p.setDisplayName(displayName);
		p.setLocation(data.get("location").getTextValue());
		return p;
	}
}
