package org.ironrhino.core.security.csrf.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.security.csrf.CsrfManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("csrfManager")
public class DefaultCsrfManager implements CsrfManager {

	@Autowired
	protected CacheManager cacheManager;

	public static final int CACHE_TOKEN_TIME_TO_LIVE = 3600;

	@Override
	public String createToken(HttpServletRequest request) {
		String token = UUID.randomUUID().toString().replace("-", "");
		cacheManager.put(request.getSession().getId(), token, -1,
				CACHE_TOKEN_TIME_TO_LIVE, KEY_CSRF);
		return token;
	}

	@Override
	public boolean validateToken(HttpServletRequest request) {
		String value = request.getParameter(KEY_CSRF);
		if (StringUtils.isBlank(value))
			return false;
		String last = (String)cacheManager.get(request.getSession().getId(),
				KEY_CSRF);
		boolean b = value.equals(last);
		cacheManager.delete(request.getSession().getId(), KEY_CSRF);
		return b;
	}

}