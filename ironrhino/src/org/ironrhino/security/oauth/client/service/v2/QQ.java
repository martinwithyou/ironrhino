package org.ironrhino.security.oauth.client.service.v2;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth2Provider;
import org.springframework.beans.factory.annotation.Value;

//@Named("qq")
//@Singleton
public class QQ extends OAuth2Provider {

	@Value("${qq.logo:http://qzonestyle.gtimg.cn/qzone/vas/opensns/res/img/Connect_logo_5.png}")
	private String logo;

	@Value("${qq.authorizeUrl:https://graph.qq.com/oauth2.0/authorize}")
	private String authorizeUrl;

	@Value("${qq.accessTokenEndpoint:https://graph.qq.com/oauth2.0/token}")
	private String accessTokenEndpoint;

	@Value("${qq.scope:}")
	private String scope;

	@Value("${qq.profileUrl:https://graph.qq.com/oauth2.0/me}")
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
		return false;
	}

	protected String getAccessTokenParameterName() {
		return "access_token";
	}

	@Override
	protected Profile getProfileFromContent(String content) throws Exception {
		content = content.substring(content.indexOf('(') + 1,
				content.lastIndexOf(')'));
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		Profile p = new Profile();
		p.setUid(data.get("openid").getTextValue());
		return p;
	}

	protected void postProccessProfile(Profile p, String accessToken)
			throws Exception {
		String uid = p.getUid();
		p.setUid(generateUid(uid));
		String content = invoke(accessToken,
				"https://graph.qq.com/user/get_info?oauth_consumer_key="
						+ getClientId() + "&openid=" + uid);
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		p.setDisplayName(data.get("nick").getTextValue());
		p.setName(data.get("name").getTextValue());
		p.setPicture(data.get("head").getTextValue());
		p.setLocation(data.get("location").getTextValue());
		p.setGender(data.get("sex").getTextValue());
		p.setEmail(data.get("email").getTextValue());
	}

}
