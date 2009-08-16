package org.ironrhino.ums.service;

import org.apache.commons.logging.LogFactory;
import org.ironrhino.ums.model.User;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadUsernameOrPasswordAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.Assert;


public final class CasAuthenticationHandler extends
		AbstractUsernamePasswordAuthenticationHandler {

	private UserManager userManager;

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public CasAuthenticationHandler() {
		log = LogFactory.getLog(getClass());
	}

	protected void afterPropertiesSetInternal() throws Exception {
		Assert.notNull(userManager, "userManager cannot be null.");
	}

	@Override
	protected boolean authenticateUsernamePasswordInternal(
			UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		User user = userManager.getByNaturalId("username", credentials
				.getUsername());
		if (user == null)
			throw new BadUsernameOrPasswordAuthenticationException();
		if (!user.isEnabled())
			throw new UnsupportedCredentialsException();
		if (!user.isAccountNonLocked())
			throw new UnsupportedCredentialsException();
		if (!user.isAccountNonExpired())
			throw new UnsupportedCredentialsException();
		if (!user.isCredentialsNonExpired())
			throw new UnsupportedCredentialsException();
		boolean success = user.isPasswordValid(credentials.getPassword());
		if (!success)
			throw new BadUsernameOrPasswordAuthenticationException();
		log.info("'" + credentials.getUsername() + "' login "
				+ (success ? "successfully" : "failed"));
		return success;
	}
}