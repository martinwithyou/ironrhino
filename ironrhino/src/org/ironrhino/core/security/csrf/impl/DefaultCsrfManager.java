package org.ironrhino.core.security.csrf.impl;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.security.csrf.CsrfManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("csrfManager")
public class DefaultCsrfManager implements CsrfManager {

	@Autowired
	protected CacheManager cacheManager;

	@Override
	public String createToken(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String validateToken(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
