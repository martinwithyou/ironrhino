package org.ironrhino.core.session.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.session.SessionCompressor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Singleton
@Named
public class SecurityContextSessionCompressor implements
		SessionCompressor<SecurityContext> {

	@Inject
	private UserDetailsService userDetailsService;

	@Override
	public boolean supportsKey(String key) {
		return HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
				.equals(key);
	}

	@Override
	public String compress(SecurityContext sc) {
		if (sc != null) {
			Authentication auth = sc.getAuthentication();
			if (auth != null && auth.isAuthenticated())
				return auth.getName();
		}
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
