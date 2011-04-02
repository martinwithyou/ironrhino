package org.ironrhino.security.oauth.client.model;

public class OAuth10aToken implements java.io.Serializable {

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
