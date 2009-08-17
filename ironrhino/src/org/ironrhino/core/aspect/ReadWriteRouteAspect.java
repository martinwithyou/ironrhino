package org.ironrhino.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.ext.spring.ReadWriteRouteDataSource;
import org.ironrhino.core.metadata.Readonly;
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
	public Object determineReadonlyByTransactional(ProceedingJoinPoint jp,
			Transactional transactional) throws Throwable {
		if (transactional != null && transactional.readOnly())
			ReadWriteRouteDataSource.setReadonly(true);
		try {
			return jp.proceed();
		} finally {
			ReadWriteRouteDataSource.setReadonly(false);
		}
	}

	@Around("execution(public * *(..)) and @annotation(readonly)")
	public Object determineReadonly(ProceedingJoinPoint jp, Readonly readonly)
			throws Throwable {
		if (readonly != null)
			ReadWriteRouteDataSource.setReadonly(true);
		try {
			return jp.proceed();
		} finally {
			ReadWriteRouteDataSource.setReadonly(false);
		}
	}

}
