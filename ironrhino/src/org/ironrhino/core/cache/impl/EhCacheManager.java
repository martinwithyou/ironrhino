package org.ironrhino.core.cache.impl;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;

public class EhCacheManager implements CacheManager {

	private net.sf.ehcache.CacheManager ehCacheManager;

	public EhCacheManager(net.sf.ehcache.CacheManager ehCacheManager) {
		this.ehCacheManager = ehCacheManager;
	}

	public void put(Serializable key, Serializable value, int timeToIdle,
			int timeToLive, String namespace) {
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			ehCacheManager.addCache(namespace);
		cache = ehCacheManager.getCache(namespace);
		cache.put(new Element(key, value, null, timeToIdle > 0 ? Integer
				.valueOf(timeToIdle) : null,
				timeToIdle <= 0 && timeToLive > 0 ? Integer.valueOf(timeToLive)
						: null));
	}

	public Serializable get(Serializable key, String namespace) {
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		Element element = cache.get(key);
		return element != null ? element.getValue() : null;

	}

	public void remove(Serializable key, String namespace) {
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			cache.remove(key);
	}

}
