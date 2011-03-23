package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.security.socialauth.Profile;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.springframework.beans.factory.annotation.Value;

@Named("sina")
@Singleton
public class Sina extends ScribeOAuthProvider {

	@Value("${sina.requestTokenUrl:http://api.t.sina.com.cn/oauth/request_token}")
	private String requestTokenUrl;

	@Value("${sina.authorizeUrl:http://api.t.sina.com.cn/oauth/authorize}")
	private String authorizeUrl;

	@Value("${sina.accessTokenUrl:http://api.t.sina.com.cn/oauth/access_token}")
	private String accessTokenUrl;

	@Value("${sina.logo:http://i1.sinaimg.cn/home/deco/2009/0330/logo_home.gif}")
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
	protected Profile doGetProfile(Token accessToken) throws Exception {
		OAuthRequest request = new OAuthRequest(Verb.GET,
				"http://api.t.sina.com.cn/account/verify_credentials.json");
		oauthService.signRequest(accessToken, request);
		Response response = request.send();
		String json = response.getBody();
		JsonNode data = mapper.readValue(json, JsonNode.class);
		String uid = String.valueOf(data.get("id").getLongValue());
		String name = data.get("name").getTextValue();
		String displayName = data.get("screen_name").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setName(name);
		p.setDisplayName(displayName);
		p.setLocation(data.get("location").getTextValue());
		p.setImage(data.get("profile_image_url").getTextValue());
		return p;
	}
}
