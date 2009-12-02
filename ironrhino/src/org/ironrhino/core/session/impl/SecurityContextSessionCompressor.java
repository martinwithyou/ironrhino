package org.ironrhino.core.session.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.session.SessionCompressor;
import org.springframework.security.Authentication;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

@Singleton
@Named
public class SecurityContextSessionCompressor implements
		SessionCompressor<SecurityContext> {

	@Inject
	private UserDetailsService userDetailsService;

	@Override
	public boolean supportsKey(String key) {
		return HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY
				.equals(key);
	}

	@Override
	public String compress(SecurityContext sc) {
		Authentication auth = sc.getAuthentication();
		if (auth != null && auth.isAuthenticated())
			return auth.getName();
		return null;
	}

	@Override
	public SecurityContext uncompress(String username) {
		if (username != null) {
			UserDetails ud = null;
			try {
				ud = userDetailsService.loadUserByUsername(username);
			} catch (UsernameNotFoundException e) {
				return null;
			}
			if (ud != null) {
				SecurityContext sc = new SecurityContextImpl();
				Authentication auth = new UsernamePasswordAuthenticationToken(
						ud, ud.getPassword(), ud.getAuthorities());
				sc.setAuthentication(auth);
				return sc;
			}
		}
		return null;
	}
}
