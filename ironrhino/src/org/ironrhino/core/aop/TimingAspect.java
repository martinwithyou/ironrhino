package org.ironrhino.core.aop;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Singleton
@Named
public class TimingAspect extends BaseAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public TimingAspect() {
		order = -1000;
	}

	@Around("execution(public * *(..)) and @annotation(timing)")
	public Object timing(ProceedingJoinPoint jp, Timing timing)
			throws Throwable {
		long time = System.currentTimeMillis();
		Object result = jp.proceed();
		time = System.currentTimeMillis() - time;
		String method = jp.getStaticPart().getSignature().toLongString();
		logger.info("{} takes {} ms", method, time);
		if (StringUtils.isNotBlank(timing.value())) {
			Map<String, Object> ctx = new HashMap<String, Object>();
			ctx.put("method", method);
			ctx.put("time", time);
			eval(timing.value(), jp, result, ctx);
		}
		return result;
	}

}
