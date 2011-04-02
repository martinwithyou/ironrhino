package org.ironrhino.security.oauth.client.service.v10a;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.oauth.client.model.OAuth10aToken;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuth10aProvider;
import org.springframework.beans.factory.annotation.Value;

@Named("qq")
@Singleton
public class QQ extends OAuth10aProvider {

	@Value("${qq.requestTokenUrl:https://open.t.qq.com/cgi-bin/request_token}")
	private String requestTokenUrl;

	@Value("${qq.authorizeUrl:https://open.t.qq.com/cgi-bin/authorize}")
	private String authorizeUrl;

	@Value("${qq.accessTokenUrl:https://open.t.qq.com/cgi-bin/access_token}")
	private String accessTokenUrl;

	@Value("${qq.logo:http://www.qq.com/images/logo.gif}")
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

	public boolean isUseAuthorizationHeader() {
		return false;
	}

	public boolean isIncludeCallbackWhenRequestToken() {
		return true;
	}

	@Override
	protected Profile doGetProfile(OAuth10aToken token) throws Exception {
		String json = invoke(token,
				"http://open.t.qq.com/api/user/info?format=json");
		JsonNode data = JsonUtils.getObjectMapper()
				.readValue(json, JsonNode.class).get("data");
		JsonNode node = data.get("uid");
		String uid = null;
		String name = data.get("name").getTextValue();
		if (node != null)
			uid = node.getTextValue();
		else
			uid = name;
		String displayName = data.get("nick").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setName(displayName);
		p.setDisplayName(displayName);
		p.setLocation(data.get("location").getTextValue());
		p.setImage(data.get("head").getTextValue());
		return p;
	}

}
