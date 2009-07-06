package org.ironrhino.core.session;

import java.util.Map;

import net.sf.ehcache.jcache.JCache;
import net.sf.jsr107cache.Cache;

import org.ironrhino.core.cache.CacheContext;

public class CacheSessionStore implements SessionStore {

	public static final String CACHE_NAME = "_session_";

	public void initialize(Session session) {
		Cache sessionCache = CacheContext.getCache(CACHE_NAME);
		Map attrMap = (Map) sessionCache.get(session.getId());
		if (attrMap != null)
			session.setAttrMap(attrMap);
	}

	public void save(Session session) {
		Cache sessionCache = CacheContext.getCache(CACHE_NAME);
		JCache jcache = (JCache) sessionCache;
		// TODO timeToIdle
		jcache.put(session.getId(), session.getAttrMap(), 30 * 60);
	}

	public void invalidate(Session session) {
		CacheContext.getCache(CACHE_NAME).remove(session.getId());
	}

}
