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
public class FrequencyAspect extends BaseAspect {

	@Inject
	private FrequencyService frequencyService;

	public FrequencyAspect() {
		order = -1000;
	}

	@Around("execution(public * *(..)) and @annotation(frequency)")
	public Object control(ProceedingJoinPoint jp, Frequency frequency)
			throws Throwable {
		Map<String, Object> context = buildContext(jp);
		String key = jp.getSignature().toLongString();
		int limits = ExpressionUtils.evalInt(frequency.limits(), context, 0);
		if (frequencyService.available(key, limits) > 1) {
			frequencyService.increment(key, 1, frequency.duration(),
					frequency.timeUnit());
			return jp.proceed();
		} else {
			throw new ErrorMessage("no available quota for @Frequency");
		}

	}

}
