package org.ironrhino.core.security.role;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
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
