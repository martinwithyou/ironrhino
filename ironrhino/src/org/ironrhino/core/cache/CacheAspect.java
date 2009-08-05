package org.ironrhino.core.cache;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import net.sf.ehcache.jcache.JCache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.common.util.ExpressionUtils;
import org.ironrhino.core.aspect.AopContext;
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

	private static Log log = LogFactory.getLog(CacheAspect.class);

	private int order;

	@Around("@annotation(checkCache)")
	public Object get(ProceedingJoinPoint call, CheckCache checkCache)
			throws Throwable {
		net.sf.jsr107cache.Cache cache = CacheContext.getCache(checkCache
				.name());
		String key = checkKey(call, checkCache);
		if (cache == null || key == null
				|| AopContext.isBypass(this.getClass()))
			return call.proceed();
		if (CacheContext.forceFlush()) {
			cache.remove(key);
		} else {
			Object value = cache.get(key);
			if (value != null) {
				return value;
			}
		}
		Object result = call.proceed();
		if (result != null && needCache(checkCache, call, result)) {
			JCache jcache = (JCache) cache;
			// TODO timeToIdle
			jcache.put(key, result, checkCache.timeToLive());
		}
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		net.sf.jsr107cache.Cache cache = CacheContext.getCache(flushCache
				.name(), false);
		String[] keys = flushKeys(jp, flushCache);
		if (AopContext.isBypass(this.getClass()) || cache == null
				|| keys == null)
			return;
		for (String key : keys)
			cache.remove(key.trim());
	}

	private boolean needCache(CheckCache checkCache, JoinPoint jp, Object result) {
		try {
			// need not cache
			String when = checkCache.when();
			if (StringUtils.isBlank(when)
					|| eval(when, jp, result).trim().equalsIgnoreCase("true"))
				return true;
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	private static String checkKey(JoinPoint jp, CheckCache cache) {
		try {
			return eval(cache.value(), jp, null).trim();
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private static String[] flushKeys(JoinPoint jp, FlushCache cache) {
		String keys;
		try {
			keys = eval(cache.value(), jp, null);
			return keys.split(",");
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

	private static String eval(String template, JoinPoint jp, Object retval)
			throws ScriptException {
		if (template.indexOf('$') < 0)
			return template;
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("_this", jp.getThis());
		if (retval != null)
			context.put("retval", retval);
		context.put("args", jp.getArgs());
		return ExpressionUtils.render(template, context);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
