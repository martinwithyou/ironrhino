package org.ironrhino.security.service;

import org.ironrhino.core.service.BaseManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.ironrhino.security.model.User;

public interface UserManager extends BaseManager<User>, UserDetailsService {

	public void save(User user);

	public String suggestUsername(String candidate);

}
