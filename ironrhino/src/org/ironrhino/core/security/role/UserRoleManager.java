package org.ironrhino.core.security.role;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.util.ClassScaner;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

@Singleton
@Named
public class UserRoleManager {

	private Set<String> staticRoles;

	@Autowired(required = false)
	private List<UserRoleProvider> userRoleProviders;

	public Set<String> getStaticRoles(boolean excludeBuiltin) {
		Set<String> roles = getStaticRoles();
		if (excludeBuiltin) {
			Set<String> set = new LinkedHashSet<String>();
			for (String s : roles)
				if (!s.startsWith("ROLE_BUILTIN_"))
					set.add(s);
			roles = set;
		}
		return roles;
	}

	public Set<String> getStaticRoles() {
		if (staticRoles == null) {
			Set<String> temp = new LinkedHashSet<String>();
			Set<Class<?>> set = ClassScaner.scanAssignable(
					ClassScaner.getAppPackages(), UserRole.class);
			for (Class<?> c : set) {
				if (Enum.class.isAssignableFrom(c)) {
					for (Object en : c.getEnumConstants()) {
						temp.add(en.toString());
					}
				} else {
					Field[] fields = c.getDeclaredFields();
					for (Field f : fields) {
						temp.add(f.getName());
					}
				}
			}
			staticRoles = Collections.unmodifiableSet(temp);
		}
		return staticRoles;
	}

	public Map<String, String> getCustomRoles() {
		Map<String, String> customRoles = new LinkedHashMap<String, String>();
		if (userRoleProviders != null)
			for (UserRoleProvider p : userRoleProviders) {
				Map<String, String> map = p.getRoles();
				if (map != null)
					customRoles.putAll(map);
			}
		return customRoles;
	}

	public Map<String, String> getAllRoles(boolean excludeBuiltin) {
		Set<String> staticRoles = getStaticRoles(excludeBuiltin);
		Map<String, String> customRoles = getCustomRoles();
		Map<String, String> roles = new LinkedHashMap<String, String>();
		for (String role : staticRoles)
			roles.put(role, LocalizedTextUtil.findText(getClass(), role,
					ActionContext.getContext().getLocale(), role, null));
		for (Map.Entry<String, String> entry : customRoles.entrySet()) {
			String value = StringUtils.isNotBlank(entry.getValue()) ? entry
					.getValue() : entry.getKey();
			roles.put(entry.getKey(), LocalizedTextUtil.findText(getClass(),
					value, ActionContext.getContext().getLocale(), value, null));
		}
		return roles;
	}

	public List<String> displayRoles(Collection<String> roles) {
		if (roles == null)
			return Collections.emptyList();
		Map<String, String> rolesMap = getAllRoles(false);
		List<String> result = new ArrayList<String>(roles.size());
		for (String s : roles) {
			String name = rolesMap.get(s);
			result.add(StringUtils.isNotBlank(name) ? name : s);
		}
		return result;
	}

	public String displayRole(String role) {
		if (StringUtils.isBlank(role))
			return "";
		Map<String, String> rolesMap = getAllRoles(false);
		String name = rolesMap.get(role);
		return StringUtils.isNotBlank(name) ? name : role;
	}
}
