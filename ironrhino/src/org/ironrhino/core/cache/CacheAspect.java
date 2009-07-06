package org.ironrhino.core.cache;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.common.util.ExpressionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired(required = false)
	private CacheManager cacheManager;

	@PostConstruct
	public void init() {
		if (cacheManager == null)
			log.warn("No CacheManager congfigured");
		CacheContext.setCacheManager(cacheManager);
	}

	@Around("@annotation(checkCache)")
	public Object get(ProceedingJoinPoint call, CheckCache checkCache)
			throws Throwable {
		Cache cache = CacheContext.getCache(checkCache.name());
		String key = checkKey(call, checkCache);
		if (CacheContext.isBypass() || cache == null || key == null)
			return call.proceed();
		if (CacheContext.forceFlush()) {
			cache.remove(key);
		} else {
			Element element = cache.get(key);
			if (element != null) {
				return element.getValue();
			}
		}
		Object result = call.proceed();
		Element element = new Element(key, result);
		element.setTimeToLive(checkCache.timeToLive());
		element.setTimeToIdle(checkCache.timeToIdle());
		cache.put(element);
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		Cache cache = CacheContext.getCache(flushCache.name(), false);
		String[] keys = flushKeys(jp, flushCache);
		if (CacheContext.isBypass() || cache == null || keys == null)
			return;
		for (String key : keys)
			cache.remove(key.trim());
	}

	private String checkKey(JoinPoint jp, CheckCache cache) {
		try {
			return eval(cache.value(), jp.getArgs()).trim();
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private String[] flushKeys(JoinPoint jp, FlushCache cache) {
		String keys;
		try {
			keys = eval(cache.value(), jp.getArgs());
			return keys.split(",");
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

	private String eval(String template, Object[] args) throws ScriptException {
		if (template.indexOf('$') < 0)
			return template;
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("args", args);
		context.put("arguments", args);
		return ExpressionUtils.render(template, context);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
