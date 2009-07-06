package org.ironrhino.core.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.jcache.JCache;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

public class CacheContext {

	private static Log log = LogFactory.getLog(CacheContext.class);

	public static final String FORCE_FLUSH_PARAM_NAME = "_ff_";

	public static final int DEFAULT_TIME_TO_LIVE = 3600 * 24;

	public static final int DEFAULT_TIME_TO_IDLE = 3600;

	public static final String DEFAULT_SCOPE = "application";

	public static final String DEFAULT_CACHE_NAME = "_default_";

	public static final String PAGE_FRAGMENT_CACHE_NAME = "_page_fragment_";

	private static Lock lock = new ReentrantLock();

	private static ThreadLocal<Boolean> bypass = new ThreadLocal<Boolean>();

	public static Cache getCache(String name) {
		return getCache(name, true);
	}

	public static Cache getCache(String name, boolean autoCreate) {
		Cache cache = null;
		CacheManager singletonManager = net.sf.jsr107cache.CacheManager
				.getInstance();
		cache = singletonManager.getCache(name);
		if (!autoCreate && cache == null)
			return null;
		if (cache != null)
			return cache;
		Map config = new HashMap();
		config.put("name", name);
		config.put("maxElementsInMemory", String.valueOf(1000000));
		config.put("timeToLiveSeconds", String.valueOf(DEFAULT_TIME_TO_LIVE));
		config.put("timeToIdleSeconds", String.valueOf(DEFAULT_TIME_TO_IDLE));
		try {
			lock.lock();
			cache = singletonManager.getCache(name);
			if (cache == null) {
				cache = singletonManager.getCacheFactory().createCache(config);
				singletonManager.registerCache(name, cache);
			}
			return cache;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
			lock.unlock();
		}
	}

	public static void setBypass() {
		bypass.set(true);
	}

	public static boolean isBypass() {
		Boolean b = bypass.get();
		boolean bl = b != null && b.booleanValue();
		bypass.set(false);
		return bl;
	}

	public static boolean forceFlush() {
		try {
			return ServletActionContext.getRequest() != null
					&& ServletActionContext.getRequest().getParameter(
							FORCE_FLUSH_PARAM_NAME) != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static String getPageFragment(String key, String scope) {
		try {
			Cache cache = getCache(PAGE_FRAGMENT_CACHE_NAME);
			if (CacheContext.forceFlush() || cache == null)
				return null;
			String content = (String) cache.get(completeKey(key, scope));
			if (content != null)
				return content;
			return null;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static void putPageFragment(String key, String content,
			String scope, int timeToLive, int timeToIdle) {
		Cache cache = getCache(PAGE_FRAGMENT_CACHE_NAME);
		if (cache == null)
			return;
		try {
			JCache jcache = (JCache) cache;
			// TODO timeToIdle
			jcache.put(completeKey(key, scope), content, timeToLive);
		} catch (Throwable e) {
		}
	}

	private static String completeKey(String key, String scope) {
		HttpServletRequest request = ServletActionContext.getRequest();
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		if (scope.equalsIgnoreCase("session"))
			sb.append("," + request.getSession(true).getId());
		return sb.toString();
	}

}
