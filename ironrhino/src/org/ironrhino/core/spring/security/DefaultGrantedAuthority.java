package org.ironrhino.core.spring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

//kryo deserialize need no-arg constructor
public class DefaultGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = -1433659911466023724L;

	private String role;

	public DefaultGrantedAuthority() {

	}

	public DefaultGrantedAuthority(String role) {
		Assert.hasText(role,
				"A granted authority textual representation is required");
		this.role = role;
	}

	public String getAuthority() {
		return role;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof DefaultGrantedAuthority) {
			return role.equals(((DefaultGrantedAuthority) obj).role);
		}

		return false;
	}

	public int hashCode() {
		return this.role.hashCode();
	}

	public String toString() {
		return this.role;
	}
}
