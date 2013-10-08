package org.ironrhino.core.security.dynauth;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@Singleton
@Named("dynamicAuthorizerManager")
public class DynamicAuthorizerManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private List<DynamicAuthorizer> authorizers;

	public boolean authorize(Class<?> authorizer, UserDetails user,
			String resource) {
		return authorize(authorizer.getName(), user, resource);
	}

	public boolean authorize(String authorizer, UserDetails user,
			String resource) {
		if (authorizers != null) {
			for (DynamicAuthorizer entry : authorizers) {
				if (entry.getClass().getName().equals(authorizer))
					return entry.authorize(user, resource);
			}
			logger.error(
					"not found authorizer [{}] in spring applicationContext",
					authorizer);
		}
		return false;
	}

}
