package org.ironrhino.core.cache;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.aop.BaseAspect;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.springframework.beans.factory.annotation.Value;

@Aspect
@Singleton
@Named
public class CacheAspect extends BaseAspect {

	private final static String MUTEX = "_MUTEX_";

	private final static int DEFAULT_MUTEX_WAIT = 200;

	@Inject
	private CacheManager cacheManager;

	@Value("${cacheAspect.mutex:false}")
	private boolean mutex;

	@Value("${cacheAspect.mutexWait:" + DEFAULT_MUTEX_WAIT + "}")
	private int mutexWait = DEFAULT_MUTEX_WAIT;

	public CacheAspect() {
		order = -100;
	}

	@Around("execution(public java.io.Serializable+ *(..)) and @annotation(checkCache)")
	public Object get(ProceedingJoinPoint jp, CheckCache checkCache)
			throws Throwable {
		String namespace = eval(checkCache.namespace(), jp, null).toString();
		String key = evalString(checkCache.key(), jp, null);

		if (key == null || isBypass())
			return jp.proceed();
		String keyMutex = MUTEX + key;
		if (CacheContext.isForceFlush()) {
			cacheManager.delete(key, namespace);
		} else {
			int timeToIdle = evalInt(checkCache.timeToIdle(), jp, null);
			Object value = (timeToIdle > 0 && !cacheManager
					.supportsTimeToIdle()) ? cacheManager.get(key, namespace,
					timeToIdle) : cacheManager.get(key, namespace);
			if (value != null) {
				eval(checkCache.onHit(), jp, value);
				return value;
			} else {
				if (mutex
						&& !cacheManager.putIfAbsent(keyMutex, "", 180,
								namespace)) {
					Thread.sleep(mutexWait);
					value = cacheManager.get(key, namespace);
					if (value != null) {
						eval(checkCache.onHit(), jp, value);
						return value;
					}
				}
				eval(checkCache.onMiss(), jp, null);
			}
		}
		Serializable result = (Serializable) jp.proceed();
		if (result != null && evalBoolean(checkCache.when(), jp, result)) {
			if (checkCache.eternal()) {
				cacheManager.put(key, result, 0, namespace);
			} else {
				int timeToLive = evalInt(checkCache.timeToLive(), jp, result);
				int timeToIdle = evalInt(checkCache.timeToIdle(), jp, result);
				cacheManager
						.put(key, result, timeToIdle, timeToLive, namespace);
			}
			if (mutex)
				cacheManager.delete(keyMutex, namespace);
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
		if (!flushCache.renew().equals("")) {
			Object value = eval(flushCache.renew(), jp);
			for (Object key : keys)
				cacheManager.put(key.toString(), value, 0, namespace);
		} else {
			cacheManager.mdelete(keys, namespace);
		}
		eval(flushCache.onFlush(), jp, null);
	}

}
