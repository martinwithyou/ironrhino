package org.ironrhino.core.security.role;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.security.core.userdetails.UserDetails;

@Named
@Singleton
public class UsernameRoleMapper implements UserRoleMapper {

	@Override
	public String[] map(UserDetails user) {
		return new String[] { map(user.getUsername()) };
	}

	public static String map(String username) {
		return new StringBuilder("USERNAME(").append(username).append(")")
				.toString();
	}

}
