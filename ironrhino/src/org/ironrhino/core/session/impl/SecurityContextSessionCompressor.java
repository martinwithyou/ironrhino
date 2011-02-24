package org.ironrhino.core.session.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.session.SessionCompressor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

	public boolean supportsKey(String key) {
		return HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
				.equals(key);
	}

	public String compress(SecurityContext sc) {
		if (sc != null) {
			Authentication auth = sc.getAuthentication();
			if (auth != null && auth.isAuthenticated())
				return auth.getName();
		}
		return null;
	}

	public SecurityContext uncompress(String username) {
		SecurityContext sc = SecurityContextHolder.getContext();
		try {
			UserDetails ud = userDetailsService.loadUserByUsername(username);
			if (ud.isEnabled() && ud.isAccountNonExpired()
					&& ud.isAccountNonLocked() && ud.isCredentialsNonExpired())
				sc.setAuthentication(new UsernamePasswordAuthenticationToken(
						ud, ud.getPassword(), ud.getAuthorities()));
		} catch (UsernameNotFoundException e) {
			e.printStackTrace();
		}
		return sc;
	}
}
