package org.ironrhino.security.model;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ironrhino.core.util.ClassScaner;

public interface UserRole {

	public static final String ROLE_BUILTIN_ANONYMOUS = "ROLE_BUILTIN_ANONYMOUS";
	public static final String ROLE_BUILTIN_USER = "ROLE_BUILTIN_USER";
	public static final String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	public static class UserRoleHelper {

		private static Set<String> roles;

		public static Set<String> getAllRoles() {
			if (roles == null) {
				Set<String> temp = new LinkedHashSet<String>();
				Set<Class> set = ClassScaner.scanAssignable(
						ClassScaner.getAppPackages(), UserRole.class);
				for (Class c : set) {
					if (Enum.class.isAssignableFrom(c)) {
						for (Object en : c.getEnumConstants()) {
							temp.add(en.toString());
						}
					} else {
						Field[] fields = c.getDeclaredFields();
						for (Field f : fields) {
							if (f.getName().startsWith("ROLE_BUILTIN_"))
								continue;
							temp.add(f.getName());
						}
					}
				}
				roles = temp;
			}
			return roles;
		}
	}

}
