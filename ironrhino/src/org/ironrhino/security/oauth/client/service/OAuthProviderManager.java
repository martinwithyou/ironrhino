package org.ironrhino.security.oauth.client.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

@Named
@Singleton
public class OAuthProviderManager {

	@Inject
	private ApplicationContext ctx;

	private Collection<OAuthProvider> providers;

	@PostConstruct
	public void afterPropertiesSet() {
		providers = ctx.getBeansOfType(OAuthProvider.class).values();
	}

	public List<OAuthProvider> getProviders() {
		List<OAuthProvider> list = new ArrayList<OAuthProvider>(
				providers.size());
		for (OAuthProvider p : providers)
			if (p.isEnabled())
				list.add(p);
		Collections.sort(list);
		return list;
	}

	public OAuthProvider lookup(String id) {
		if (StringUtils.isBlank(id))
			return null;
		for (OAuthProvider p : providers)
			if (p.isEnabled() && id.equals(p.getName()))
				return p;
		return null;
	}

}
