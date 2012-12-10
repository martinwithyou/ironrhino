package org.ironrhino.security.oauth.client.service.v1;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth1Provider;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;

@Named("netease")
@Singleton
public class Netease extends OAuth1Provider {

	@Value("${netease.requestTokenUrl:http://api.t.163.com/oauth/request_token}")
	private String requestTokenUrl;

	@Value("${netease.authorizeUrl:http://api.t.163.com/oauth/authenticate}")
	private String authorizeUrl;

	@Value("${netease.accessTokenUrl:http://api.t.163.com/oauth/access_token}")
	private String accessTokenUrl;

	@Value("${netease.logo:http://img3.cache.netease.com/t/img10/index/logo.png}")
	private String logo;

	@Value("${netease.profileUrl:http://api.t.163.com/account/verify_credentials.json}")
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
		String uid = data.get("id").textValue();
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