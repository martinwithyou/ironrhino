package org.ironrhino.security.oauth.client.service.v2;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth2Provider;
import org.springframework.beans.factory.annotation.Value;

@Named("weibo")
@Singleton
public class Weibo extends OAuth2Provider {

	@Value("${weibo.logo:http://timg.sjs.sinajs.cn/t35/appstyle/opent/images/app/logo_zx.png}")
	private String logo;

	@Value("${weibo.authorizeUrl:https://api.weibo.com/oauth2/authorize}")
	private String authorizeUrl;

	@Value("${weibo.accessTokenEndpoint:https://api.weibo.com/oauth2/access_token}")
	private String accessTokenEndpoint;

	@Value("${weibo.scope:}")
	private String scope;

	@Value("${weibo.profileUrl:https://api.weibo.com/2/account/get_uid.json}")
	private String profileUrl;

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
	public String getProfileUrl() {
		return profileUrl;
	}

	public String getAccessKey() {
		return settingControl.getStringValue("oauth." + getName()
				+ ".accessKey");
	}

	@Override
	public boolean isUseAuthorizationHeader() {
		return true;
	}

	protected String getAuthorizationHeaderType() {
		return "OAuth2";
	}

	@Override
	protected Profile getProfileFromContent(String content) throws Exception {
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		Profile p = new Profile();
		String uid = data.get("uid").getTextValue();
		p.setUid(uid);
		return p;
	}

	protected void postProcessProfile(Profile p, String accessToken)
			throws Exception {
		String uid = p.getUid();
		p.setUid(generateUid(uid));
		String content = invoke(accessToken,
				"https://api.weibo.com/2/users/show.json?uid=" + uid);
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		p.setDisplayName(data.get("screen_name").getTextValue());
		p.setName(data.get("name").getTextValue());
		p.setLocation(data.get("location").getTextValue());
		p.setLink(data.get("url").getTextValue());
		p.setPicture(data.get("profile_image_url").getTextValue());
	}

}
