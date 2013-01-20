package org.ironrhino.core.security.dynauth;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;

@Singleton
@Named("dynamicAuthorizerManager")
public class DynamicAuthorizerManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Collection<DynamicAuthorizer> authorizers;

	@Inject
	private ApplicationContext ctx;

	@PostConstruct
	public void init() {
		authorizers = ctx.getBeansOfType(DynamicAuthorizer.class).values();
	}

	public boolean authorize(Class<?> authorizer, UserDetails user,
			String resource) {
		return authorize(authorizer.getName(), user, resource);
	}

	public boolean authorize(String authorizer, UserDetails user,
			String resource) {
		for (DynamicAuthorizer entry : authorizers) {
			if (entry.getClass().getName().equals(authorizer))
				return entry.authorize(user, resource);
		}
		logger.error("not found authorizer [{}] in spring applicationContext",
				authorizer);
		return false;
	}

}
