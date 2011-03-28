package org.ironrhino.security.socialauth.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("google")
@Singleton
public class Google extends OAuth20Provider {

	@Value("${google.logo:http://www.google.com/images/logos/accounts_logo.gif}")
	private String logo;

	@Value("${google.authorizeUrl:https://accounts.google.com/o/oauth2/auth}")
	private String authorizeUrl;

	@Value("${google.accessTokenEndpoint:https://accounts.google.com/o/oauth2/token}")
	private String accessTokenEndpoint;

	private String scope = "https://www-opensocial.googleusercontent.com/api/people/";

	// private String scope =
	// "https://www-opensocial.googleusercontent.com/api/people/ https://www.google.com/m8/feeds/";

	public String getLogo() {
		return logo;
	}

	@Override
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return accessTokenEndpoint;
	}

	public String getScope() {
		return scope;
	}

	public String getAccessKey() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".accessKey");
	}

	@Override
	protected Profile doGetProfile(String token) throws Exception {
		String content = invoke(token,
				"http://www-opensocial.googleusercontent.com/api/people/@me/@self");
		JsonNode data = mapper.readValue(content, JsonNode.class);
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

	@Override
	protected String invoke(String protectedURL, Map<String, String> params,
			Map<String, String> headers) {
		if (params == null)
			params = new HashMap<String, String>();
		params.put("key", getAccessKey());
		return super.invoke(protectedURL, params, headers);
	}

}
