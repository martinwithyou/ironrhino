package org.ironrhino.security.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.cache.CheckCache;
import org.ironrhino.core.cache.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@Named("userManager")
public class UserManagerImpl extends BaseManagerImpl<User> implements
		UserManager {

	@Inject
	private ApplicationContext ctx;

	private Collection<UserRoleMapper> mappers;

	@PostConstruct
	public void afterPropertiesSet() {
		mappers = ctx.getBeansOfType(UserRoleMapper.class).values();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canDelete(User obj) {
		return !obj.isEnabled();
	}

	@Override
	@Transactional
	@FlushCache(namespace = "user", key = "${[user.username,user.email]}")
	public void delete(User user) {
		super.delete(user);
	}

	@Override
	@Transactional
	@FlushCache(namespace = "user", key = "${[user.username,user.email]}")
	public void save(User user) {
		super.save(user);
	}

	@Transactional(readOnly = true)
	@CheckCache(namespace = "user", key = "${username}")
	public User loadUserByUsername(String username) {
		if (StringUtils.isEmpty(username))
			return null;
		username = username.toLowerCase();
		User user;
		if (username.indexOf('@') > 0)
			user = findByNaturalId("email", username);
		else
			user = findByNaturalId(username);
		if (user == null)
			throw new UsernameNotFoundException("No such Username");
		populateAuthorities(user);
		return user;
	}

	private void populateAuthorities(User user) {
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		auths.add(new SimpleGrantedAuthority(UserRole.ROLE_BUILTIN_ANONYMOUS));
		auths.add(new SimpleGrantedAuthority(UserRole.ROLE_BUILTIN_USER));
		for (String role : user.getRoles())
			auths.add(new SimpleGrantedAuthority(role));
		if (mappers != null)
			for (UserRoleMapper mapper : mappers) {
				String[] roles = mapper.map(user);
				if (roles != null)
					for (String role : roles)
						auths.add(new SimpleGrantedAuthority(role));
			}
		user.setAuthorities(auths);
	}

	public String suggestUsername(String candidate) {
		if (candidate.indexOf("://") > 0) {
			try {
				URL url = new URL(candidate);
				String path = url.getPath();
				if (path.length() > 1) {
					candidate = path.substring(1);
					if (candidate.endsWith("/"))
						candidate = candidate.substring(0,
								candidate.length() - 1);
				} else {
					candidate = candidate
							.substring(candidate.indexOf("://") + 3);
					String temp = candidate
							.substring(0, candidate.indexOf('.'));
					if (!temp.equalsIgnoreCase("www")) {
						candidate = temp;
					} else {
						candidate = candidate
								.substring(candidate.indexOf('.') + 1);
						candidate = candidate.substring(0,
								candidate.indexOf('.'));
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		int i = candidate.indexOf('@');
		if (i > 0)
			candidate = candidate.substring(0, i);
		candidate.replace('.', '_');
		User user = findByNaturalId(candidate);
		if (user == null)
			return candidate;
		i = 10;
		int digits = 1;
		i = CodecUtils.randomInt(digits);
		user = findByNaturalId(candidate + i);
		while (user != null) {
			digits++;
			i = CodecUtils.randomInt(digits);
			user = findByNaturalId(candidate + i);
		}
		return candidate + i;
	}

}
