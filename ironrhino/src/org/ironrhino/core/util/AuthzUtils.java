package org.ironrhino.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.model.Secured;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthzUtils {

	public static Object authentication(String property) {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (auth.getPrincipal() == null)
			return null;
		try {
			BeanWrapperImpl wrapper = new BeanWrapperImpl(auth);
			return wrapper.getPropertyValue(property);
		} catch (BeansException e) {
			return null;
		}
	}

	public static boolean authorize(String ifAllGranted, String ifAnyGranted,
			String ifNotGranted, String expression) {
		if (StringUtils.isNotBlank(expression)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("user", getUserDetails(UserDetails.class));
			Object o = ExpressionUtils.eval(expression, map);
			return o != null && o.toString().equals("true");
		}
		List<String> roles = getRoleNames();
		if (StringUtils.isNotBlank(ifAllGranted)) {
			String[] arr = ifAllGranted.split(",");
			for (String s : arr)
				if (!roles.contains(s.trim()))
					return false;
			return true;
		} else if (StringUtils.isNotBlank(ifAnyGranted)) {
			String[] arr = ifAnyGranted.split(",");
			for (String s : arr)
				if (roles.contains(s.trim()))
					return true;
			return false;
		} else if (StringUtils.isNotBlank(ifNotGranted)) {
			String[] arr = ifNotGranted.split(",");
			boolean b = true;
			for (String s : arr)
				if (roles.contains(s.trim())) {
					b = false;
					break;
				}
			return b;
		}
		return false;
	}

	public static List<String> getRoleNames() {
		List<String> roleNames = new ArrayList<String>();
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			Collection<GrantedAuthority> authz = SecurityContextHolder
					.getContext().getAuthentication().getAuthorities();
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
		for (String s : entity.getRoles()) {
			if (roleNames.contains(s))
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

	public static void autoLogin(UserDetails ud) {
		SecurityContext sc = SecurityContextHolder.getContext();
		Authentication auth = new UsernamePasswordAuthenticationToken(ud,
				ud.getPassword(), ud.getAuthorities());
		sc.setAuthentication(auth);
	}

	public static String encodePassword(UserDetails ud, String input) {
		PasswordEncoder encoder = ApplicationContextUtils
				.getBean(PasswordEncoder.class);
		SaltSource ss = ApplicationContextUtils.getBean(SaltSource.class);
		return encoder
				.encodePassword(input, ss != null ? ss.getSalt(ud) : null);
	}

	public static boolean isPasswordValid(UserDetails ud, String password) {
		PasswordEncoder encoder = ApplicationContextUtils
				.getBean(PasswordEncoder.class);
		SaltSource ss = ApplicationContextUtils.getBean(SaltSource.class);
		return encoder.isPasswordValid(ud.getPassword(), password,
				ss != null ? ss.getSalt(ud) : null);
	}

	public static boolean isPasswordValid(String password) {
		Object principal = SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		return principal instanceof UserDetails ? isPasswordValid(
				(UserDetails) principal, password) : false;
	}
}