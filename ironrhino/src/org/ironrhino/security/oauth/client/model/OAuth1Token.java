package org.ironrhino.security.oauth.client.model;

public class OAuth1Token extends OAuthToken {

	private static final long serialVersionUID = 1320442804849307877L;

	private String token;

	private String secret;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

}
