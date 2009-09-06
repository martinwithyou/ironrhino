package org.ironrhino.ums.service;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.ums.model.User;
import org.springframework.security.userdetails.UserDetailsService;

public interface UserManager extends BaseManager<User>, UserDetailsService {

	public static final String ROLE_BUILTIN_ANONYMOUS = "ROLE_BUILTIN_ANONYMOUS";

	public static final String ROLE_BUILTIN_USER = "ROLE_BUILTIN_USER";

	public void save(User user);

	public User loadUserByUsername(String username);

	public User getByUsername(String username);

	public User getByEmail(String email);

	public String suggestName(String candidate);

	public boolean isActivationRequired(String email);

	public void deleteDisabled();

}
