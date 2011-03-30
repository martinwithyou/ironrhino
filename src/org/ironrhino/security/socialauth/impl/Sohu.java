package org.ironrhino.security.socialauth.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("sohu")
@Singleton
public class Sohu extends OAuth10aProvider {

	@Value("${sohu.requestTokenUrl:http://api.t.sohu.com/oauth/request_token}")
	private String requestTokenUrl;

	@Value("${sohu.authorizeUrl:http://api.t.sohu.com/oauth/authorize}")
	private String authorizeUrl;

	@Value("${sohu.accessTokenUrl:http://api.t.sohu.com/oauth/access_token}")
	private String accessTokenUrl;

	@Value("${sohu.logo:http://s1.cr.itc.cn/img/t/logo_sp6.png}")
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
		"http://api.t.sohu.com/account/verify_credentials.json");
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
