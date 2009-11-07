package org.ironrhino.core.session.impl;

import java.io.Serializable;
import java.util.Map;

import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.session.Constants;
import org.ironrhino.core.session.HttpWrappedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("sessionManager")
public class CacheBasedSessionManager extends AbstractSessionManager {

	@Autowired
	private CacheManager cacheManager;

	public void doInitialize(HttpWrappedSession session) {
		Map attrMap = (Map) cacheManager.get(session.getId(),
				Constants.CACHE_NAMESPACE);
		if (attrMap != null && attrMap.size() > 0)
			session.setAttrMap(attrMap);
	}

	public void doSave(HttpWrappedSession session) {
		Map attrMap = session.getAttrMap();
		if (attrMap != null && attrMap.size() > 0)
			cacheManager.put(session.getId(), (Serializable) session
					.getAttrMap(), session.getMaxInactiveInterval(), -1,
					Constants.CACHE_NAMESPACE);
	}

	public void doInvalidate(HttpWrappedSession session) {
		cacheManager.delete(session.getId(), Constants.CACHE_NAMESPACE);
	}

}
