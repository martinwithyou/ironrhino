package org.ironrhino.security.oauth;

import java.io.Serializable;

import org.springframework.security.core.userdetails.UserDetails;

public class Authorization implements Serializable {

	private UserDetails user;

	private String[] scopes;

	public UserDetails getUser() {
		return user;
	}

	public void setUser(UserDetails user) {
		this.user = user;
	}

	public String[] getScopes() {
		return scopes;
	}

	public void setScopes(String[] scopes) {
		this.scopes = scopes;
	}

}
