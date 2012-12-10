package org.ironrhino.security.oauth.client.model;

import org.ironrhino.core.metadata.NotInJson;

public class OAuth2Token extends OAuthToken {

	private static final long serialVersionUID = 3664222731669918663L;
	private String access_token;
	private String token_type;
	private int expires_in;
	private long create_time;
	private String refresh_token;

	public OAuth2Token() {
		create_time = System.currentTimeMillis();
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public int getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	@NotInJson
	public boolean isExpired() {
		if (expires_in <= 0 || create_time <= 0)
			return false;
		int offset = 60;
		return (System.currentTimeMillis() - create_time) / 1000 > (expires_in - offset);
	}

	@Override
	public String toString() {
		return "OAuth2Token [access_token=" + access_token + ", token_type="
				+ token_type + ", expires_in=" + expires_in + ", create_time="
				+ create_time + ", refresh_token=" + refresh_token + "]";
	}

}