package org.ironrhino.security.socialauth.impl;

import org.ironrhino.security.socialauth.AuthProvider;

public abstract class AbstractAuthProvider implements AuthProvider {

	public String getName() {
		return getClass().getSimpleName().toLowerCase();
	}

}
