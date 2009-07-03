package org.ironrhino.online.service;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.Account;
import org.springframework.security.userdetails.UserDetailsService;

public interface AccountManager extends BaseManager<Account>,
		UserDetailsService {

	public static final String ROLE_BUILTIN_ANONYMOUS = "ROLE_BUILTIN_ANONYMOUS";
	public static final String ROLE_BUILTIN_ACCOUNT = "ROLE_BUILTIN_ACCOUNT";

	public void save(Account account);

	public Account getAccountByUsername(String username);

	public Account getAccountByEmail(String email);

	public Account getAccountByOpenid(String openid);

	// public Account loadUserByOpenid(String openid);

	public void deleteDisabledAccount();

	public String suggestUsername(String email);
}
