package org.ironrhino.core.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.jcache.JCache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;
import ognl.OgnlContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.mvel2.templates.TemplateRuntime;

import com.opensymphony.xwork2.ActionContext;

public class CacheContext {

	private static Log log = LogFactory.getLog(CacheContext.class);

	public static final String FORCE_FLUSH_PARAM_NAME = "_ff_";

	public static final String DEFAULT_TIME_TO_LIVE = "3600";

	public static final String DEFAULT_TIME_TO_IDLE = "-1";

	public static final String DEFAULT_SCOPE = "application";

	public static final String DEFAULT_CACHE_NAMESPACE = "default";

	public static final String PAGE_FRAGMENT_CACHE_NAMESPACE = "page";

	private static Lock lock = new ReentrantLock();

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
		config.put("memoryStoreEvictionPolicy", String
				.valueOf(MemoryStoreEvictionPolicy.LRU));
		config.put("maxElementsInMemory", String.valueOf(1000000));
		config.put("timeToLiveSeconds", DEFAULT_TIME_TO_LIVE);
		config.put("timeToIdleSeconds", DEFAULT_TIME_TO_IDLE);
		config.put("overflowToDisk", String.valueOf(true));
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

	public static boolean isForceFlush() {
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
			Object actualKey = eval(key);
			Cache cache = getCache(PAGE_FRAGMENT_CACHE_NAMESPACE);
			if (actualKey == null || CacheContext.isForceFlush()
					|| cache == null)
				return null;
			key = actualKey.toString();
			scope = eval(scope).toString();
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
			String scope, String timeToLive, String timeToIdle) {
		Cache cache = getCache(PAGE_FRAGMENT_CACHE_NAMESPACE);
		Object actualKey = eval(key);
		if (actualKey == null || cache == null)
			return;
		try {
			JCache jcache = (JCache) cache;
			key = actualKey.toString();
			scope = eval(scope).toString();
			int _timeToLive = Integer.valueOf(eval(timeToLive).toString());
			int _timeToIdle = Integer.valueOf(eval(timeToIdle).toString());
			Ehcache ehcache = jcache.getBackingCache();
			ehcache.put(new Element(completeKey(key, scope), content, null,
					_timeToIdle > 0 ? Integer.valueOf(_timeToIdle) : null,
					_timeToIdle <= 0 && _timeToLive > 0 ? Integer
							.valueOf(_timeToLive) : null));
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

	public static Object eval(String template) {
		if (template == null)
			return null;
		template = template.trim();
		OgnlContext ognl = (OgnlContext) ActionContext.getContext()
				.getContextMap();
		Object value = TemplateRuntime.eval(template, ognl.getValues());
		return value;
	}

}
