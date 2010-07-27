package org.ironrhino.security.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@Named("userManager")
public class UserManagerImpl extends BaseManagerImpl<User> implements
		UserManager {

	@Override
	@Transactional(readOnly = true)
	public boolean canDelete(User obj) {
		return !obj.isEnabled();
	}

	@Override
	@Transactional
	@FlushCache(namespace = "user", key = "${args[0].username}")
	public void save(User user) {
		super.save(user);
	}

	@Transactional(readOnly = true)
	@CheckCache(namespace = "user", key = "${args[0]}")
	public User loadUserByUsername(String username) {
		username = username.toLowerCase();
		User user = findByNaturalId(username);
		if (user == null)
			throw new UsernameNotFoundException("No such Username");
		populateAuthorities(user);
		return user;
	}

	private void populateAuthorities(User user) {
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		auths.add(new GrantedAuthorityImpl(UserRole.ROLE_BUILTIN_ANONYMOUS));
		auths.add(new GrantedAuthorityImpl(UserRole.ROLE_BUILTIN_USER));
		for (String role : user.getRoles())
			auths.add(new GrantedAuthorityImpl(role));
		user.setAuthorities(auths);
	}

}
