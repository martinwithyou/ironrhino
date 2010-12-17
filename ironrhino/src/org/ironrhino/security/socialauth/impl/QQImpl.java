package org.ironrhino.security.socialauth.impl;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.ironrhino.security.socialauth.Profile;
import org.springframework.beans.factory.annotation.Value;

@Named("qq")
@Singleton
public class QQImpl extends AbstractOAuthProvider {

	@Value("${qq.requestTokenUrl:https://open.t.qq.com/cgi-bin/request_token}")
	private String requestTokenUrl;

	@Value("${qq.authorizeUrl:https://open.t.qq.com/cgi-bin/authorize}")
	private String authorizeUrl;

	@Value("${qq.accessTokenUrl:https://open.t.qq.com/cgi-bin/access_token}")
	private String accessTokenUrl;

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
	protected Profile doGetProfile(Map<String, String> token) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		OAuthClient client = new OAuthClient(new HttpClient4());
		OAuthMessage message = client.invoke(accessor, 
				"http://open.t.qq.com/api/user/info?format=json", token.entrySet());
		String xml = message.readBodyAsString();
		System.out.println(xml);
		return null;
	}
	
	/*
{
ret:0,
Msg:"ok",
Data:{
Name:"abc",
Nick:"abcd",
Uid:"xxxxxxxxx",
Head:"",
Location:"广东 深圳",
Country_code:"1",
Province_code:"44",
City_code:"3",
isVip:0,
Isent:0,
Introduction:"",
verifyInfo:"",
Birth_year:"1984"
Birth_month:"3"
Birth_day:"28"
Sex:1
Fansnum:100,
Idolnum:100,
Tweetnum:100
Tag:[{Id:1,name:""},{id:2,name:""},....]
}
}
	 */

}
