package org.ironrhino.core.security.rrm;

import org.springframework.security.core.userdetails.UserDetails;

public interface ResourceRoleMapper {

	public String map(String resource, UserDetails user);

}
