package org.ironrhino.core.cache;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

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

	private static ThreadLocal<Boolean> bypass = new ThreadLocal<Boolean>();

	static CacheManager cacheManager;

	public static void setCacheManager(CacheManager cm) {
		cacheManager = cm;
	}

	public static Cache getCache(String name) {
		return getCache(name, true);
	}

	public static Cache getCache(String name, boolean autoCreate) {
		if (cacheManager == null)
			return null;
		Cache cache = cacheManager.getCache(name);
		if (autoCreate && cache == null) {
			cache = new Cache(name, 100000, true, true, DEFAULT_TIME_TO_LIVE,
					DEFAULT_TIME_TO_IDLE);
			if (!cacheManager.cacheExists(name))
				cacheManager.addCache(cache);
		}
		return cache;
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
			Element element = cache.get(completeKey(key, scope));
			if (element != null)
				return (String) element.getValue();
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
			Element element = new Element(completeKey(key, scope), content);
			if (timeToIdle > 0)
				element.setTimeToIdle(timeToIdle);
			if (timeToLive > 0)
				element.setTimeToLive(timeToLive);
			cache.put(element);
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
