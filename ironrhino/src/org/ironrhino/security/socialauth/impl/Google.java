package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.security.socialauth.Profile;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.springframework.beans.factory.annotation.Value;

@Named("google")
@Singleton
public class Google extends ScribeOAuthProvider {

	@Value("${google.logo:http://www.google.com/images/logos/accounts_logo.gif}")
	private String logo;

	public String getLogo() {
		return logo;
	}

	@Override
	public String getRequestTokenUrl() {
		return null;
	}

	@Override
	public String getAuthorizeUrl() {
		return null;
	}

	@Override
	public String getAccessTokenUrl() {
		return null;
	}

	@Override
	public DefaultApi10a getApi() {
		return new GoogleApi();
	}

	@Override
	protected Profile doGetProfile(Token accessToken) throws Exception {
		OAuthRequest request = new OAuthRequest(Verb.GET,
				"http://www-opensocial.googleusercontent.com/api/people/@me/@self");
		oauthService.signRequest(accessToken, request);
		Response response = request.send();
		String json = response.getBody();
		JsonNode data = mapper.readValue(json, JsonNode.class);
		JsonNode entry = data.get("entry");
		String uid = entry.get("id").getTextValue();
		String name = entry.get("name").get("formatted").getTextValue();
		String displayName = data.get("displayName").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setName(name);
		p.setDisplayName(displayName);
		// p.setLocation(data.get("location").getTextValue());
		p.setImage(entry.get("thumbnailUrl").getTextValue());
		return p;
	}

}
