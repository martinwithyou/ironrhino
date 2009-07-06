package org.ironrhino.core.session;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.ironrhino.core.cache.CacheContext;

public class CacheSessionStore implements SessionStore {

	public static final String CACHE_NAME = "_session_";

	public void initialize(Session session) {
		Cache sessionCache = CacheContext.getCache(CACHE_NAME);
		if (sessionCache.isKeyInCache(session.getId()))
			session.setAttrMap((Map) sessionCache.get(session.getId())
					.getValue());
	}

	public void save(Session session) {
		CacheContext.getCache(CACHE_NAME).put(
				new Element(session.getId(), session.getAttrMap(), false,
						15 * 60, -1));
	}

	public void invalidate(Session session) {
		CacheContext.getCache(CACHE_NAME).remove(session.getId());
	}

}
