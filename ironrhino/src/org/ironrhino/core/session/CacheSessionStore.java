package org.ironrhino.core.session;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class CacheSessionStore implements SessionStore {

	private Cache sessionCache;

	public void setSessionCache(Cache sessionCache) {
		this.sessionCache = sessionCache;
	}

	public void initialize(Session session) {
		if (sessionCache.isKeyInCache(session.getId()))
			session.setAttrMap((Map) sessionCache.get(session.getId())
					.getValue());
	}

	public void save(Session session) {
		sessionCache.put(new Element(session.getId(), session.getAttrMap(),
				false, 15 * 60, -1));
	}

	public void invalidate(Session session) {
		sessionCache.remove(session.getId());
	}

}
