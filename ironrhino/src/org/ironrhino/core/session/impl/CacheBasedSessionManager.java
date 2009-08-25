package org.ironrhino.core.session.impl;

import java.util.Map;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.jcache.JCache;
import net.sf.jsr107cache.Cache;

import org.ironrhino.core.cache.CacheContext;
import org.ironrhino.core.session.HttpWrappedSession;
import org.ironrhino.core.session.HttpSessionManager;

public class CacheBasedSessionManager implements HttpSessionManager {

	public static final String CACHE_NAME = "session";

	public void initialize(HttpWrappedSession session) {
		Cache sessionCache = CacheContext.getCache(CACHE_NAME);
		Map attrMap = (Map) sessionCache.get(session.getId());
		if (attrMap != null)
			session.setAttrMap(attrMap);
	}

	public void save(HttpWrappedSession session) {
		Cache sessionCache = CacheContext.getCache(CACHE_NAME);
		JCache jcache = (JCache) sessionCache;
		Ehcache cache = jcache.getBackingCache();
		cache.put(new Element(session.getId(), session.getAttrMap(), null, session
				.getMaxInactiveInterval(), null));
	}

	public void invalidate(HttpWrappedSession session) {
		CacheContext.getCache(CACHE_NAME).remove(session.getId());
	}

}
