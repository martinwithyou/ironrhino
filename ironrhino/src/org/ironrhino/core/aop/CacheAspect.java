package org.ironrhino.core.aop;

import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.jcache.JCache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.cache.CacheContext;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;

/**
 * cache some data
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.metadata.CheckCache
 * @see org.ironrhino.core.metadata.FlushCache
 */
@Aspect
public class CacheAspect extends BaseAspect {

	private Log log = LogFactory.getLog(CacheAspect.class);

	@Around("execution(public java.io.Serializable+ *(..)) and @annotation(checkCache)")
	public Object get(ProceedingJoinPoint jp, CheckCache checkCache)
			throws Throwable {
		String name = eval(checkCache.namespace(), jp, null).toString();
		List keys = checkKeys(jp, checkCache);
		net.sf.jsr107cache.Cache cache = CacheContext.getCache(name);
		if (cache == null || keys == null || keys.size() == 0 || isBypass())
			return jp.proceed();
		if (CacheContext.isForceFlush()) {
			for (Object key : keys)
				cache.remove(key);
		} else {
			Object value = null;
			for (Object key : keys) {
				if ((value = cache.get(key)) != null)
					break;
			}
			if (value != null) {
				eval(checkCache.onHit(), jp, value);
				return value;
			} else {
				eval(checkCache.onMiss(), jp, null);
			}
		}
		Object result = jp.proceed();
		if (result != null && needCache(checkCache, jp, result)) {
			JCache jcache = (JCache) cache;
			int timeToLive = Integer.valueOf(eval(checkCache.timeToLive(), jp,
					result).toString());
			int timeToIdle = Integer.valueOf(eval(checkCache.timeToIdle(), jp,
					result).toString());
			Ehcache ehcache = jcache.getBackingCache();
			for (Object key : keys)
				ehcache.put(new Element(key, result, null,
						timeToIdle > 0 ? Integer.valueOf(timeToIdle) : null,
						timeToIdle <= 0 && timeToLive > 0 ? Integer
								.valueOf(timeToLive) : null));
			eval(checkCache.onPut(), jp, result);
		}
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		String name = eval(flushCache.namespace(), jp, null).toString();
		net.sf.jsr107cache.Cache cache = CacheContext.getCache(name, false);
		List keys = flushKeys(jp, flushCache);
		if (isBypass() || cache == null || keys == null || keys.size() == 0)
			return;
		for (Object key : keys)
			if (key != null)
				cache.remove(key.toString().trim());
		eval(flushCache.onFlush(), jp, null);
	}

	private boolean needCache(CheckCache checkCache, JoinPoint jp, Object result) {
		try {
			// need not cache
			String when = checkCache.when();
			if (StringUtils.isBlank(when)
					|| String.valueOf(eval(when, jp, result)).equalsIgnoreCase(
							"true"))
				return true;
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	private List checkKeys(JoinPoint jp, CheckCache cache) {
		try {
			Object keys = eval(cache.key(), jp, null);
			if (keys == null)
				return null;
			if (keys instanceof List)
				return (List) keys;
			return Arrays.asList(keys.toString().split(","));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private List flushKeys(JoinPoint jp, FlushCache cache) {
		try {
			Object keys = eval(cache.key(), jp, null);
			if (keys == null)
				return null;
			if (keys instanceof List)
				return (List) keys;
			return Arrays.asList(keys.toString().split(","));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

}
