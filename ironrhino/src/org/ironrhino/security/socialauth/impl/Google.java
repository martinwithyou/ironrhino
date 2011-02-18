package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("google")
@Singleton
public class Google extends AbstractOAuthProvider {

	@Value("${google.requestTokenUrl:https://www.google.com/accounts/OAuthGetRequestToken}")
	private String requestTokenUrl;

	@Value("${google.authorizeUrl:https://www.google.com/accounts/OAuthAuthorizeToken}")
	private String authorizeUrl;

	@Value("${google.accessTokenUrl:https://www.google.com/accounts/OAuthGetAccessToken}")
	private String accessTokenUrl;

	@Value("${google.logo:http://www.google.com/images/logos/accounts_logo.gif}")
	private String logo;

	public String getLogo() {
		return logo;
	}

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
		OAuthMessage message = client
				.invoke(
						accessor,
						"http://www-opensocial.googleusercontent.com/api/people/@me/@self",
						null);
		String json = message.readBodyAsString();
		JsonNode data = mapper.readValue(json, JsonNode.class);
		JsonNode entry = data.get("entry");
		String uid = entry.get("id").getTextValue();
		String name = entry.get("name").get("formatted").getTextValue();
		String displayName = data.get("displayName").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setName(name);
		p.setDisplayName(displayName);
//		p.setLocation(data.get("location").getTextValue());
		p.setImage(entry.get("thumbnailUrl").getTextValue());
		return p;
	}

}
