package org.ironrhino.core.session.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.SessionCompressorManager;
import org.ironrhino.core.session.WrappedHttpSession;

@Singleton
@Named("cacheBased")
public class CacheBasedHttpSessionStore implements HttpSessionStore {

	public static final String CACHE_NAMESPACE = "session";

	@Inject
	private SessionCompressorManager sessionCompressorManager;

	@Inject
	private CacheManager cacheManager;

	@Override
	public void initialize(WrappedHttpSession session) {
		sessionCompressorManager.uncompress(session, (String) cacheManager.get(
				session.getId(), CACHE_NAMESPACE));
	}

	@Override
	public void save(WrappedHttpSession session) {
		String sessionString = sessionCompressorManager.compress(session);
		if (session.isDirty() && StringUtils.isBlank(sessionString)) {
			cacheManager.delete(session.getId(), CACHE_NAMESPACE);
			return;
		}
		if (cacheManager.supportsTimeToIdle()) {
			if (session.isDirty())
				cacheManager.put(session.getId(), sessionString, session
						.getMaxInactiveInterval(), -1, CACHE_NAMESPACE);
		} else {
			if (session.isDirty()
					|| session.getNow() - session.getLastAccessedTime() > session
							.getMinActiveInterval() * 1000)
				cacheManager.put(session.getId(), sessionString, session
						.getMaxInactiveInterval(), CACHE_NAMESPACE);
		}
	}

	@Override
	public void invalidate(WrappedHttpSession session) {
		cacheManager.delete(session.getId(), CACHE_NAMESPACE);
	}

}
