package org.ironrhino.core.session.impl;

import java.io.Serializable;
import java.util.Map;

import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.WrappedHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cacheBased")
public class CacheBasedHttpSessionStore implements HttpSessionStore {

	public static final String CACHE_NAMESPACE = "session";

	@Autowired
	private CacheManager cacheManager;

	public void initialize(WrappedHttpSession session) {
		Map attrMap = (Map) cacheManager.get(session.getId(), CACHE_NAMESPACE);
		if (attrMap != null && attrMap.size() > 0)
			session.setAttrMap(attrMap);
	}

	public void save(WrappedHttpSession session) {
		Map attrMap = session.getAttrMap();
		if (attrMap != null && attrMap.size() > 0)
			cacheManager.put(session.getId(), (Serializable) session
					.getAttrMap(), session.getMaxInactiveInterval(), -1,
					CACHE_NAMESPACE);
	}

	public void invalidate(WrappedHttpSession session) {
		cacheManager.delete(session.getId(), CACHE_NAMESPACE);
	}

}
