package org.ironrhino.core.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.ext.spring.ReadWriteRouteDataSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * split read and write database
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.ext.spring.ReadWriteRouteDataSource
 */
@Aspect
public class ReadWriteRouteAspect extends BaseAspect {

	@Around("execution(public * *(..)) and @annotation(transactional)")
	public Object determineReadonly(ProceedingJoinPoint jp,
			Transactional transactional) throws Throwable {
		if (ReadWriteRouteDataSource.isReadonly())
			return jp.proceed();
		if (transactional.readOnly())
			ReadWriteRouteDataSource.setReadonly(true);
		try {
			return jp.proceed();
		} finally {
			ReadWriteRouteDataSource.setReadonly(false);
		}
	}

}
