package org.ironrhino.core.aop;

import java.io.Serializable;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.cache.CacheContext;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * cache some data
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.metadata.CheckCache
 * @see org.ironrhino.core.metadata.FlushCache
 */
@Aspect
public class CacheAspect extends BaseAspect {

	@Autowired
	private CacheManager cacheManager;

	@Around("execution(public java.io.Serializable+ *(..)) and @annotation(checkCache)")
	public Object get(ProceedingJoinPoint jp, CheckCache checkCache)
			throws Throwable {
		String namespace = eval(checkCache.namespace(), jp, null).toString();
		List<Serializable> keys = evalList(checkCache.key(), jp, null);
		if (keys == null || keys.size() == 0 || isBypass())
			return jp.proceed();
		if (CacheContext.isForceFlush()) {
			for (Serializable key : keys)
				cacheManager.delete(key, namespace);
		} else {
			Object value = null;
			for (Serializable key : keys) {
				if ((value = cacheManager.get(key, namespace)) != null)
					break;
			}
			if (value != null) {
				eval(checkCache.onHit(), jp, value);
				return value;
			} else {
				eval(checkCache.onMiss(), jp, null);
			}
		}
		Serializable result = (Serializable) jp.proceed();
		if (result != null && evalBoolean(checkCache.when(), jp, result)) {
			int timeToLive = evalInt(checkCache.timeToLive(), jp, result);
			int timeToIdle = evalInt(checkCache.timeToIdle(), jp, result);
			for (Serializable key : keys)
				cacheManager
						.put(key, result, timeToIdle, timeToLive, namespace);
			eval(checkCache.onPut(), jp, result);
		}
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		String namespace = eval(flushCache.namespace(), jp, null).toString();
		List<Serializable> keys = evalList(flushCache.key(), jp, null);
		if (isBypass() || keys == null || keys.size() == 0)
			return;
		cacheManager.mdelete(keys, namespace);
		eval(flushCache.onFlush(), jp, null);
	}

}
