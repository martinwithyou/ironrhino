package org.ironrhino.core.aop;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ironrhino.core.metadata.Async;
import org.ironrhino.core.metadata.ConcurrencyControl;
import org.springframework.stereotype.Component;

/**
 * 
 * @author zhouyanming
 */
@Aspect
@Component
public class ConcurrentAspect extends BaseAspect {

	private ConcurrentHashMap<String, Semaphore> map = new ConcurrentHashMap<String, Semaphore>();

	private int threadPoolSize = 20;

	private ExecutorService executorService;

	public ConcurrentAspect() {
		order = -1000;
	}

	@PostConstruct
	public void afterPropertiesSet() {
		executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	@Around("execution(public * *(..)) and @annotation(concurrencyControl)")
	public Object control(ProceedingJoinPoint jp,
			ConcurrencyControl concurrencyControl) throws Throwable {
		String key = jp.getSignature().toLongString();
		Semaphore semaphore = map.get(key);
		if (semaphore == null) {
			semaphore = new Semaphore(evalInt(concurrencyControl.permits(), jp,
					null), evalBoolean(concurrencyControl.fair(), jp, null));
			map.put(key, semaphore);
		}
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

	@Around("execution(public * *(..)) and @annotation(async)")
	public Object async(ProceedingJoinPoint jp, Async async) throws Throwable {
		final Object _this = jp.getTarget();
		final Object[] args = jp.getArgs();
		final Method method = ((MethodSignature) jp.getSignature()).getMethod();
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					method.invoke(_this, args);
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
				}

			}
		});
		return null;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

}
