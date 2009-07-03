package org.ironrhino.core.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

/**
 * cache some data
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.annotation.Cache
 */
@Aspect
public class CacheAspect implements Ordered {

	private int order;

	private Cache methodCache;

	@Around("execution(public java.io.Serializable *(..)) and @annotation(org.ironrhino.core.annotation.Cache)")
	public Object get(ProceedingJoinPoint call) throws Throwable {
		String key = key(call);
		if (key == null)
			return call.proceed();
		if (methodCache != null) {
			Element element = methodCache.get(key);
			if (element != null)
				return element.getValue();
		}
		Object result = call.proceed();
		if (methodCache != null) {
			Element element = new Element(key, result);
			methodCache.put(element);
		}

		return result;
	}

	@AfterReturning("@annotation(org.ironrhino.core.annotation.Cache)")
	public void remove(JoinPoint jp) {
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	private String key(ProceedingJoinPoint call) {
		// TODO

		return null;
	}

	public void setMethodCache(Cache methodCache) {
		this.methodCache = methodCache;
	}

}
