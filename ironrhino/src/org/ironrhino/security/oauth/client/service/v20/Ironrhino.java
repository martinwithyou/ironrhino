package org.ironrhino.security.oauth.client.service.v20;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth20Provider;
import org.springframework.beans.factory.annotation.Value;

@Named("ironrhino")
@Singleton
public class Ironrhino extends OAuth20Provider {

	@Value("${ironrhino.logo:http://localhost/assets/images/logo.gif}")
	private String logo;

	@Value("${ironrhino.authorizeUrl:http://localhost/oauth2/auth}")
	private String authorizeUrl;

	@Value("${ironrhino.accessTokenEndpoint:http://localhost/oauth2/token}")
	private String accessTokenEndpoint;

	private String scope = "http://localhost/";

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

	public String getAccessKey() {
		return settingControl.getStringValue("oauth." + getName()
				+ ".accessKey");
	}

	@Override
	public boolean isUseAuthorizationHeader() {
		return true;
	}

	@Override
	protected Profile doGetProfile(String token) throws Exception {
		String content = invoke(token, "http://localhost/user/self");
		User user = JsonUtils.fromJson(content, User.class);
		Profile p = new Profile();
		p.setId(user.getUsername());
		p.setDisplayName(user.getName());
		return p;
	}

}
