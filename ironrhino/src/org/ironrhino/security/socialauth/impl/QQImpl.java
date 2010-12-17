package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("qq")
@Singleton
public class QQImpl extends AbstractOAuthProvider {

	@Value("${qq.requestTokenUrl:https://open.t.qq.com/cgi-bin/request_token}")
	private String requestTokenUrl;

	@Value("${qq.authorizeUrl:https://open.t.qq.com/cgi-bin/authorize}")
	private String authorizeUrl;

	@Value("${qq.accessTokenUrl:https://open.t.qq.com/cgi-bin/access_token}")
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
				"http://open.t.qq.com/api/user/info?format=json", null);
		String json = message.readBodyAsString();
		JsonNode data = mapper.readValue(json, JsonNode.class).get("data");
		JsonNode node = data.get("uid");
		String uid = null;
		if (node != null)
			uid = node.getTextValue();
		else
			uid = data.get("name").getTextValue();
		String displayName = data.get("nick").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setDisplayName(displayName);
		return p;
	}

}
