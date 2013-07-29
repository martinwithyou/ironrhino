package org.ironrhino.core.throttle;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.aop.BaseAspect;
import org.ironrhino.core.util.ErrorMessage;
import org.ironrhino.core.util.ExpressionUtils;

@Aspect
@Singleton
@Named
public class ConcurrencyAspect extends BaseAspect {

	@Inject
	private ConcurrencyService concurrencyService;

	public ConcurrencyAspect() {
		order = -1000;
	}

	@Around("execution(public * *(..)) and @annotation(concurrencyControl)")
	public Object control(ProceedingJoinPoint jp,
			Concurrency concurrencyControl) throws Throwable {
		Map<String, Object> context = buildContext(jp);
		String key = jp.getSignature().toLongString();
		int permits = ExpressionUtils.evalInt(concurrencyControl.permits(),
				context, 0);
		if (!concurrencyControl.block()) {
			if (concurrencyService
					.tryAcquire(key, permits, concurrencyControl.timeout(),
							concurrencyControl.timeUnit())) {
				try {
					return jp.proceed();
				} finally {
					concurrencyService.release(key);
				}
			} else {
				throw new ErrorMessage(
						"no available permit for @ConcurrencyControl");
			}
		} else {
			concurrencyService.acquire(key, permits);
			try {
				return jp.proceed();
			} finally {
				concurrencyService.release(key);
			}
		}

	}

}
