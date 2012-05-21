package org.ironrhino.security.oauth.client.service.v1;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth1Provider;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;

@Named("sina")
@Singleton
public class Sina extends OAuth1Provider {

	@Value("${sina.requestTokenUrl:http://api.t.sina.com.cn/oauth/request_token}")
	private String requestTokenUrl;

	@Value("${sina.authorizeUrl:http://api.t.sina.com.cn/oauth/authorize}")
	private String authorizeUrl;

	@Value("${sina.accessTokenUrl:http://api.t.sina.com.cn/oauth/access_token}")
	private String accessTokenUrl;

	@Value("${sina.logo:http://timg.sjs.sinajs.cn/t35/appstyle/opent/images/app/logo_zx.png}")
	private String logo;

	@Value("${sina.profileUrl:http://api.t.sina.com.cn/account/verify_credentials.json}")
	private String profileUrl;

	@Override
	public String getProfileUrl() {
		return profileUrl;
	}

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
	protected Profile getProfileFromContent(String content) throws Exception {
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		String uid = String.valueOf(data.get("id").asLong());
		String name = data.get("name").textValue();
		String displayName = data.get("screen_name").textValue();
		Profile p = new Profile();
		p.setUid(generateUid(uid));
		p.setName(name);
		p.setDisplayName(displayName);
		p.setLocation(data.get("location").textValue());
		p.setPicture(data.get("profile_image_url").textValue());
		return p;
	}
}
