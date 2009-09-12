package org.ironrhino.core.session.impl;

import java.io.Serializable;
import java.util.Map;

import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpWrappedSession;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheBasedSessionManager implements HttpSessionManager {

	public static final String CACHE_NAMESPACE = "session";

	@Autowired
	private CacheManager cacheManager;

	public void initialize(HttpWrappedSession session) {
		Map attrMap = (Map) cacheManager.get(session.getId(), CACHE_NAMESPACE);
		if (attrMap != null)
			session.setAttrMap(attrMap);
	}

	public void save(HttpWrappedSession session) {
		cacheManager.put(session.getId(), (Serializable) session.getAttrMap(),
				session.getMaxInactiveInterval(), -1, CACHE_NAMESPACE);
	}

	public void invalidate(HttpWrappedSession session) {
		cacheManager.remove(session.getId(), CACHE_NAMESPACE);
	}

}
