package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("netease")
@Singleton
public class Netease extends OAuth10aProvider  {

	@Value("${netease.requestTokenUrl:http://api.t.163.com/oauth/request_token}")
	private String requestTokenUrl;

	@Value("${netease.authorizeUrl:http://api.t.163.com/oauth/authenticate}")
	private String authorizeUrl;

	@Value("${netease.accessTokenUrl:http://api.t.163.com/oauth/access_token}")
	private String accessTokenUrl;

	@Value("${netease.logo:http://img3.cache.netease.com/t/img10/index/logo.png}")
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
	protected Profile doGetProfile(OAuth10aToken token) throws Exception {
		String json = invoke(token,
		"http://api.t.163.com/account/verify_credentials.json");
		JsonNode data = JsonUtils.getObjectMapper().readValue(json, JsonNode.class);
		String uid = data.get("id").getTextValue();
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
