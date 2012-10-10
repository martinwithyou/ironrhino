package org.ironrhino.security.service;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.security.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserManager extends BaseManager<User>, UserDetailsService {

	public void save(User user);

	public void delete(User user);

	public String suggestUsername(String candidate);

}
