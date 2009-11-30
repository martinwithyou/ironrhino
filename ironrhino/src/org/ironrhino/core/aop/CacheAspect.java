package org.ironrhino.core.aop;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.cache.CacheContext;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.springframework.stereotype.Component;

/**
 * cache some data
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.metadata.CheckCache
 * @see org.ironrhino.core.metadata.FlushCache
 */
@Aspect
@Component
public class CacheAspect extends BaseAspect {

	@Inject
	private CacheManager cacheManager;

	public CacheAspect() {
		order = -100;
	}

	@Around("execution(public java.io.Serializable+ *(..)) and @annotation(checkCache)")
	public Object get(ProceedingJoinPoint jp, CheckCache checkCache)
			throws Throwable {
		String namespace = eval(checkCache.namespace(), jp, null).toString();
		List keys = evalList(checkCache.key(), jp, null);
		if (keys == null || keys.size() == 0 || isBypass())
			return jp.proceed();
		if (CacheContext.isForceFlush()) {
			for (Object key : keys)
				cacheManager.delete(key.toString(), namespace);
		} else {
			Object value = null;
			for (Object key : keys) {
				if ((value = cacheManager.get(key.toString(), namespace)) != null)
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
			for (Object key : keys)
				cacheManager.put(key.toString(), result, timeToIdle,
						timeToLive, namespace);
			eval(checkCache.onPut(), jp, result);
		}
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		String namespace = eval(flushCache.namespace(), jp, null).toString();
		List keys = evalList(flushCache.key(), jp, null);
		if (isBypass() || keys == null || keys.size() == 0)
			return;
		cacheManager.mdelete(keys, namespace);
		eval(flushCache.onFlush(), jp, null);
	}

}
