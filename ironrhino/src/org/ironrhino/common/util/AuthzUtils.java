package org.ironrhino.common.util;

import java.util.ArrayList;
import java.util.List;

import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.core.model.Secured;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;

public class AuthzUtils {
	public static List<String> getRoleNames() {
		List<String> roleNames = new ArrayList<String>();
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			GrantedAuthority[] authz = SecurityContextHolder.getContext()
					.getAuthentication().getAuthorities();
			if (authz != null)
				for (GrantedAuthority var : authz)
					roleNames.add(var.getAuthority());
		}
		return roleNames;
	}

	public static boolean hasPermission(Secured entity) {
		if (entity == null)
			return false;
		if (entity.getRoles() == null || entity.getRoles().size() == 0)
			return true;
		List<String> roleNames = getRoleNames();
		for (SimpleElement n : entity.getRoles()) {
			if (roleNames.contains(n.getValue()))
				return true;
		}
		return false;
	}

	public static String getUsername() {
		SecurityContext sc = SecurityContextHolder.getContext();
		if (sc == null)
			return null;
		Authentication auth = sc.getAuthentication();
		if (auth == null)
			return null;
		return auth.getName();
	}

	public static <T extends UserDetails> T getUserDetails(Class<T> clazz) {
		SecurityContext sc = SecurityContextHolder.getContext();
		if (sc == null)
			return null;
		Authentication auth = sc.getAuthentication();
		if (auth == null)
			return null;
		Object principal = auth.getPrincipal();
		if (principal == null)
			return null;
		if (clazz.isAssignableFrom(principal.getClass()))
			return (T) principal;
		return null;
	}
}