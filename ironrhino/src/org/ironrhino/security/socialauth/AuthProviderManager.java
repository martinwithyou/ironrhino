package org.ironrhino.security.socialauth;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

@Named
@Singleton
public class AuthProviderManager {

	@Inject
	private ApplicationContext ctx;

	private Collection<AuthProvider> providers;

	@PostConstruct
	public void afterPropertiesSet() {
		providers = ctx.getBeansOfType(AuthProvider.class).values();
	}

	public AuthProvider lookup(String id) {
		if (StringUtils.isBlank(id))
			return null;
		String name = id;
		if (name.indexOf("://") > 0)
			name = "openid";
		for (AuthProvider p : providers)
			if (name.equals(p.getName()))
				return p;
		return null;
	}

}
