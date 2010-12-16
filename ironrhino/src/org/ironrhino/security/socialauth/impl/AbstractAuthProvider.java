package org.ironrhino.security.socialauth.impl;

import org.ironrhino.security.socialauth.AuthProvider;

public abstract class AbstractAuthProvider implements AuthProvider {

	public String getName() {
		String name = getClass().getSimpleName();
		if (name.endsWith("Impl"))
			name = name.substring(0, name.length() - 4);
		return name.toLowerCase();
	}

}
