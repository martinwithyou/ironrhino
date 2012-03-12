package org.ironrhino.security.oauth.client.model;

import org.ironrhino.core.metadata.NotInJson;

public class OAuth20Token implements java.io.Serializable {

	private static final long serialVersionUID = 3664222731669918663L;
	private String access_token;
	private String token_type;
	private int expires_in;
	private long create_time;
	private String refresh_token;
	private String id_token;

	public String getId_token() {
		return id_token;
	}

	public void setId_token(String id_token) {
		this.id_token = id_token;
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
		return System.currentTimeMillis() - create_time > (expires_in - offset) * 1000;
	}

}