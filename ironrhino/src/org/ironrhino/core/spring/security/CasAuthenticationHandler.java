package org.ironrhino.core.spring.security;

import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadUsernameOrPasswordAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

public final class CasAuthenticationHandler extends
		AbstractUsernamePasswordAuthenticationHandler {

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public CasAuthenticationHandler() {
		log = LogFactory.getLog(getClass());
	}

	@Override
	protected boolean authenticateUsernamePasswordInternal(
			UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		UserDetails userDetails = userDetailsService
				.loadUserByUsername(credentials.getUsername());
		if (userDetails == null)
			throw new BadUsernameOrPasswordAuthenticationException();
		if (!userDetails.isEnabled())
			throw new UnsupportedCredentialsException();
		if (!userDetails.isAccountNonLocked())
			throw new UnsupportedCredentialsException();
		if (!userDetails.isAccountNonExpired())
			throw new UnsupportedCredentialsException();
		if (!userDetails.isCredentialsNonExpired())
			throw new UnsupportedCredentialsException();
		boolean success = passwordEncoder.isPasswordValid(userDetails
				.getPassword(), credentials.getPassword(), null);
		if (!success)
			throw new BadUsernameOrPasswordAuthenticationException();
		log.info("'" + credentials.getUsername() + "' login "
				+ (success ? "successfully" : "failed"));
		return success;
	}
}