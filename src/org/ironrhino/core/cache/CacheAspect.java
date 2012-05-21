package org.ironrhino.core.cache;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.aop.BaseAspect;
import org.ironrhino.core.util.ExpressionUtils;
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

	@Around("execution(public * *(..)) and @annotation(checkCache)")
	public Object get(ProceedingJoinPoint jp, CheckCache checkCache)
			throws Throwable {
		Map<String, Object> context = buildContext(jp);
		String namespace = ExpressionUtils.evalString(checkCache.namespace(),
				context);
		String key = ExpressionUtils.evalString(checkCache.key(), context);
		if (key == null || isBypass())
			return jp.proceed();
		String keyMutex = MUTEX + key;
		if (CacheContext.isForceFlush()) {
			cacheManager.delete(key, namespace);
		} else {
			int timeToIdle = ExpressionUtils.evalInt(checkCache.timeToIdle(),
					context);
			Object value = (timeToIdle > 0 && !cacheManager
					.supportsTimeToIdle()) ? cacheManager.get(key, namespace,
					timeToIdle) : cacheManager.get(key, namespace);
			if (value != null) {
				putReturnValueIntoContext(context, value);
				ExpressionUtils.eval(checkCache.onHit(), context);
				return value;
			} else {
				if (mutex
						&& !cacheManager.putIfAbsent(keyMutex, "", 180,
								namespace)) {
					Thread.sleep(mutexWait);
					value = cacheManager.get(key, namespace);
					if (value != null) {
						putReturnValueIntoContext(context, value);
						ExpressionUtils.eval(checkCache.onHit(), context);
						return value;
					}
				}
				ExpressionUtils.eval(checkCache.onMiss(), context);
			}
		}
		Object result = jp.proceed();
		putReturnValueIntoContext(context, result);
		if (result != null
				&& ExpressionUtils.evalBoolean(checkCache.when(), context)) {
			if (checkCache.eternal()) {
				cacheManager.put(key, result, 0, namespace);
			} else {
				int timeToLive = ExpressionUtils.evalInt(
						checkCache.timeToLive(), context);
				int timeToIdle = ExpressionUtils.evalInt(
						checkCache.timeToIdle(), context);
				cacheManager
						.put(key, result, timeToIdle, timeToLive, namespace);
			}
			if (mutex)
				cacheManager.delete(keyMutex, namespace);
			ExpressionUtils.eval(checkCache.onPut(), context);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		Map<String, Object> context = buildContext(jp);
		String namespace = ExpressionUtils.evalString(flushCache.namespace(),
				context);
		List keys = ExpressionUtils.evalList(flushCache.key(), context);
		if (isBypass() || keys == null || keys.size() == 0)
			return;
		if (StringUtils.isNotBlank(flushCache.renew())) {
			Object value = ExpressionUtils.eval(flushCache.renew(), context);
			for (Object key : keys)
				cacheManager.put(key.toString(), value, 0, namespace);
		} else {
			cacheManager.mdelete(keys, namespace);
		}
		ExpressionUtils.eval(flushCache.onFlush(), context);
	}

}
