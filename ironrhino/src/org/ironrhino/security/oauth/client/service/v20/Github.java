package org.ironrhino.security.oauth.client.service.v20;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth20Provider;
import org.springframework.beans.factory.annotation.Value;

@Named("github")
@Singleton
public class Github extends OAuth20Provider {

	@Value("${github.logo:https://a248.e.akamai.net/assets.github.com/images/modules/header/logov5-hover.png}")
	private String logo;

	@Value("${github.authorizeUrl:https://github.com/login/oauth/authorize}")
	private String authorizeUrl;

	@Value("${github.accessTokenEndpoint:https://github.com/login/oauth/access_token}")
	private String accessTokenEndpoint;

	private String scope = "user";

	@Override
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

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public boolean isUseAuthorizationHeader() {
		return false;
	}

	@Override
	protected Profile doGetProfile(String token) throws Exception {
		String content = invoke(token, "https://api.github.com/user");
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		String uid = data.get("login").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setDisplayName(uid);
		p.setImage(data.get("avatar_url").getTextValue());
		return p;
	}

}
