package org.ironrhino.core.aop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.util.ExpressionUtils;

@Aspect
@Singleton
@Named
public class ConcurrentAspect extends BaseAspect {

	private ConcurrentHashMap<String, Semaphore> map = new ConcurrentHashMap<String, Semaphore>();

	public ConcurrentAspect() {
		order = -1000;
	}

	@Around("execution(public * *(..)) and @annotation(concurrencyControl)")
	public Object control(ProceedingJoinPoint jp,
			ConcurrencyControl concurrencyControl) throws Throwable {
		Map<String, Object> context = buildContext(jp);
		String key = jp.getSignature().toLongString();
		Semaphore semaphore = map.putIfAbsent(
				key,
				new Semaphore(ExpressionUtils.evalInt(
						concurrencyControl.permits(), context), ExpressionUtils
						.evalBoolean(concurrencyControl.fair(), context)));
		if (!concurrencyControl.block()) {
			if (semaphore.tryAcquire(concurrencyControl.timeout(),
					TimeUnit.MILLISECONDS)) {
				try {
					return jp.proceed();
				} finally {
					semaphore.release();
				}
			} else {
				throw new RuntimeException(
						"no available permit for @ConcurrencyControl");
			}
		} else {
			semaphore.acquire();
			try {
				return jp.proceed();
			} finally {
				semaphore.release();
			}
		}

	}

}
