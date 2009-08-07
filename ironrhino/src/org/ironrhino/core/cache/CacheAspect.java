package org.ironrhino.core.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.jcache.JCache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.aspect.AopContext;
import org.mvel2.templates.TemplateRuntime;
import org.springframework.core.Ordered;

/**
 * cache some data
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.cache.CheckCache
 * @see org.ironrhino.core.cache.FlushCache
 */
@Aspect
public class CacheAspect implements Ordered {

	private Log log = LogFactory.getLog(CacheAspect.class);

	private int order;

	@Around("@annotation(checkCache)")
	public Object get(ProceedingJoinPoint jp, CheckCache checkCache)
			throws Throwable {
		String name = eval(checkCache.name(), jp, null).toString();
		String key = checkKey(jp, checkCache);
		net.sf.jsr107cache.Cache cache = CacheContext.getCache(name);
		if (cache == null || key == null
				|| AopContext.isBypass(this.getClass()))
			return jp.proceed();
		if (CacheContext.isForceFlush()) {
			cache.remove(key);
		} else {
			Object value = cache.get(key);
			if (value != null) {
				return value;
			}
		}
		Object result = jp.proceed();
		if (result != null && needCache(checkCache, jp, result)) {
			JCache jcache = (JCache) cache;
			// TODO timeToIdle
			int timeToLive = Integer.valueOf(eval(checkCache.timeToLive(), jp,
					result).toString());
			jcache.put(key, result, timeToLive);
		}
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		String name = eval(flushCache.name(), jp, null).toString();
		net.sf.jsr107cache.Cache cache = CacheContext.getCache(name, false);
		List keys = flushKeys(jp, flushCache);
		if (AopContext.isBypass(this.getClass()) || cache == null
				|| keys == null)
			return;
		for (Object key : keys)
			cache.remove(key.toString().trim());
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

	private String checkKey(JoinPoint jp, CheckCache cache) {
		try {
			Object key = eval(cache.key(), jp, null);
			if (key == null)
				return null;
			return key.toString().trim();
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

	private Object eval(String template, JoinPoint jp, Object retval) {
		if (template == null)
			return null;
		template = template.trim();
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("_this", jp.getThis());
		context.put("target", jp.getTarget());
		context.put("aspect", this);
		if (retval != null)
			context.put("retval", retval);
		context.put("args", jp.getArgs());
		Object value = TemplateRuntime.eval(template, context);
		return value;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
