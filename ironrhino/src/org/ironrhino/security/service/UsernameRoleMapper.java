package org.ironrhino.security.service;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.security.model.User;

@Named
@Singleton
public class UsernameRoleMapper implements UserRoleMapper {

	public String[] map(User user) {
		return new String[] { "USERNAME(" + user.getUsername() + ")" };
	}

}
