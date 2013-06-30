package org.ironrhino.security.service;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.security.model.User;

@Named
@Singleton
public class UsernameRoleMapper implements UserRoleMapper {

	@Override
	public String[] map(User user) {
		return new String[] { map(user.getUsername()) };
	}

	public static String map(String username) {
		return new StringBuilder("USERNAME(").append(username).append(")")
				.toString();
	}

}
