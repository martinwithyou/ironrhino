package org.ironrhino.core.cache.impl;

import static org.ironrhino.core.metadata.Profiles.DEFAULT;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.springframework.context.annotation.Profile;

@Singleton
@Named("cacheManager")
@Profile(DEFAULT)
public class EhCacheManager implements CacheManager {

	private net.sf.ehcache.CacheManager ehCacheManager;

	@PostConstruct
	public void init() {
		this.ehCacheManager = new net.sf.ehcache.CacheManager();
	}

	@PreDestroy
	public void destroy() {
		ehCacheManager.shutdown();
	}

	public void put(String key, Object value, int timeToLive, String namespace) {
		put(key, value, -1, timeToLive, namespace);
	}

	public void put(String key, Object value, int timeToIdle, int timeToLive,
			String namespace) {
		if (key == null || value == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null) {
			try {
				ehCacheManager.addCache(namespace);
				cache = ehCacheManager.getCache(namespace);
			} catch (Exception e) {

			}
		}
		if (cache != null)
			cache.put(new Element(key, value, null, timeToIdle > 0 ? Integer
					.valueOf(timeToIdle) : null, timeToIdle <= 0
					&& timeToLive > 0 ? Integer.valueOf(timeToLive) : null));
	}

	public Object get(String key, String namespace) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		Element element = cache.get(key);
		return element != null ? element.getObjectValue() : null;
	}

	public Object get(String key, String namespace, int timeToLive) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		Element element = cache.get(key);
		if (element != null) {
			if (element.getTimeToIdle() != timeToLive) {
				element.setTimeToIdle(timeToLive);
				cache.put(element);
			}
			return element.getObjectValue();
		}
		return null;

	}

	public void delete(String key, String namespace) {
		if (key == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			cache.remove(key);
	}

	public void mput(Map<String, Object> map, int timeToLive, String namespace) {
		mput(map, -1, timeToLive, namespace);
	}

	public void mput(Map<String, Object> map, int timeToIdle, int timeToLive,
			String namespace) {
		if (map == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			ehCacheManager.addCache(namespace);
		cache = ehCacheManager.getCache(namespace);
		for (Map.Entry<String, Object> entry : map.entrySet())
			cache.put(new Element(entry.getKey(), entry.getValue(), null,
					timeToIdle > 0 ? Integer.valueOf(timeToIdle) : null,
					timeToIdle <= 0 && timeToLive > 0 ? Integer
							.valueOf(timeToLive) : null));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> mget(Collection<String> keys, String namespace) {
		if (keys == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache == null)
			return null;
		return cache.getAllWithLoader(keys, null);
	}

	public void mdelete(Collection<String> keys, String namespace) {
		if (keys == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			for (String key : keys)
				cache.remove(key);
	}

	public boolean containsKey(String key, String namespace) {
		if (key == null)
			return false;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		if (cache != null)
			return cache.isKeyInCache(key);
		else
			return false;
	}

	public boolean putIfAbsent(String key, Object value, int timeToLive,
			String namespace) {
		if (key == null || value == null)
			return false;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		Cache cache = ehCacheManager.getCache(namespace);
		return cache
				.putIfAbsent(new Element(key, value, null, null, timeToLive)) != null;
	}

	public boolean supportsTimeToIdle() {
		return true;
	}

	public boolean supportsUpdateTimeToLive() {
		return true;
	}
}
