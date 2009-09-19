package org.ironrhino.core.cache.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
		if (key == null || value == null)
			return;
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
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		Element element = cache.get(key);
		return element != null ? element.getValue() : null;

	}

	public void delete(Serializable key, String namespace) {
		if (key == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			cache.remove(key);
	}

	public void mput(Map<Serializable, Serializable> map, int timeToIdle,
			int timeToLive, String namespace) {
		if (map == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			ehCacheManager.addCache(namespace);
		cache = ehCacheManager.getCache(namespace);
		for (Map.Entry<Serializable, Serializable> entry : map.entrySet())
			cache.put(new Element(entry.getKey(), entry.getValue(), null,
					timeToIdle > 0 ? Integer.valueOf(timeToIdle) : null,
					timeToIdle <= 0 && timeToLive > 0 ? Integer
							.valueOf(timeToLive) : null));
	}

	public Map<Serializable, Serializable> mget(Collection<Serializable> keys,
			String namespace) {
		if (keys == null)
			return null;
		Map<Serializable, Serializable> map = new HashMap<Serializable, Serializable>();
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		for (Serializable key : keys) {
			if (key == null)
				continue;
			Element element = cache.get(key);
			map.put(key, element != null ? element.getValue() : null);
		}
		return map;
	}

	public void mdelete(Collection<Serializable> keys, String namespace) {
		if (keys == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			for (Serializable key : keys)
				cache.remove(key);
	}

}
