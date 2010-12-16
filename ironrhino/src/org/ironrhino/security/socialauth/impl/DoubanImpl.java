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

@Named
@Singleton
public class DoubanImpl extends AbstractOAuthProvider {

	@Override
	protected Profile doGetProfile(Map<String, String> token) throws Exception {
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = token.get(OAuth.OAUTH_TOKEN);
		accessor.tokenSecret = token.get(OAuth.OAUTH_TOKEN_SECRET);
		OAuthClient client = new OAuthClient(new HttpClient4());
		OAuthMessage message = client.invoke(accessor, "GET",
				"http://api.douban.com/people/%40me", token.entrySet());
		String xml = message.readBodyAsString();
		System.out.println(xml);
		return null;
	}

	/*
<?xml version="1.0" encoding="UTF-8"?>
<entry xmlns="http://www.w3.org/2005/Atom" xmlns:db="http://www.douban.com/xmlns/" xmlns:gd="http://schemas.google.com/g/2005" xmlns:openSearch="http://a9.com/-/spec/opensearchrss/1.0/" xmlns:opensearch="http://a9.com/-/spec/opensearchrss/1.0/">
        <id>http://api.douban.com/people/1823964</id>
        <title>x</title>
        <link href="http://api.douban.com/people/1823964" rel="self"/>
        <link href="http://www.douban.com/people/quaff/" rel="alternate"/>
        <link href="http://img3.douban.com/icon/user_normal.jpg" rel="icon"/>
        <content></content>
        <db:attribute name="n_mails">0</db:attribute>
        <db:attribute name="n_notifications">1</db:attribute>
        <db:signature></db:signature>
        <db:uid>quaff</db:uid>
        <uri>http://api.douban.com/people/1823964</uri>
</entry>
	 */
}
