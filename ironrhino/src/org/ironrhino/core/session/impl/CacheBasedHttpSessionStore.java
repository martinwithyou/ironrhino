package org.ironrhino.core.session.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.session.WrappedHttpSession;

@Singleton@Named("cacheBased")
public class CacheBasedHttpSessionStore extends AbstractHttpSessionStore {

	public static final String CACHE_NAMESPACE = "session";

	@Inject
	private CacheManager cacheManager;

	@Override
	public String getSessionString(WrappedHttpSession session) {
		return (String) cacheManager.get(session.getId(), CACHE_NAMESPACE);
	}

	@Override
	public void saveSessionString(WrappedHttpSession session,
			String sessionString) {
		if (StringUtils.isNotBlank(sessionString))
			cacheManager.put(session.getId(), sessionString, session
					.getMaxInactiveInterval(), -1, CACHE_NAMESPACE);
		else
			cacheManager.delete(session.getId(), CACHE_NAMESPACE);
	}

	public void invalidate(WrappedHttpSession session) {
		cacheManager.delete(session.getId(), CACHE_NAMESPACE);
	}

}
