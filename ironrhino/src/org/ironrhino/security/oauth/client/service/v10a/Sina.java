package org.ironrhino.security.oauth.client.service.v10a;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.OAuth10aToken;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth10aProvider;
import org.springframework.beans.factory.annotation.Value;

@Named("sina")
@Singleton
public class Sina extends OAuth10aProvider {

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
	protected Profile doGetProfile(OAuth10aToken token) throws Exception {
		String json = invoke(token,
				"http://api.t.sina.com.cn/account/verify_credentials.json");
		JsonNode data = JsonUtils.getObjectMapper().readValue(json, JsonNode.class);
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
