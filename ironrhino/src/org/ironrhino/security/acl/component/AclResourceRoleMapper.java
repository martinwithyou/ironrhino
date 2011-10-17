package org.ironrhino.security.acl.component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.security.rrm.ResourceRoleMapper;
import org.ironrhino.security.acl.model.Acl;
import org.ironrhino.security.acl.service.AclManager;
import org.ironrhino.security.model.User;
import org.ironrhino.security.service.UsernameRoleMapper;
import org.springframework.security.core.userdetails.UserDetails;

@Singleton
@Named
public class AclResourceRoleMapper implements ResourceRoleMapper {

	@Inject
	private UsernameRoleMapper usernameRoleMapper;

	@Inject
	private AclManager aclManager;

	public String map(String resource, UserDetails user) {
		if (user == null)
			return null;
		Acl acl = aclManager.findAcl(user.getUsername(), resource);
		return acl != null && acl.isPermitted() ? StringUtils.join(
				usernameRoleMapper.map((User) user), ",") : null;
	}

}
