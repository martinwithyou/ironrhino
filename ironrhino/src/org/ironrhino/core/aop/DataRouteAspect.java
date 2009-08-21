package org.ironrhino.core.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.dataroute.DataRouteContext;
import org.ironrhino.core.metadata.DataRoute;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.service.BaseManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * split read and write database
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.dataroute.RoutedDataSource
 * @see org.ironrhino.core.dataroute.GroupedDataSource
 */
@Aspect
public class DataRouteAspect extends BaseAspect {

	@Around("execution(public * *(..)) and @annotation(transactional)")
	public Object determineReadonly(ProceedingJoinPoint jp,
			Transactional transactional) throws Throwable {
		if (DataRouteContext.isReadonly())
			return jp.proceed();
		if (transactional.readOnly())
			DataRouteContext.setReadonly(true);
		try {
			return jp.proceed();
		} finally {
			DataRouteContext.setReadonly(false);
		}
	}

	@Around("execution(public * *(..)) and target(baseManager)")
	public Object determineGroup(ProceedingJoinPoint jp, BaseManager baseManager)
			throws Throwable {
		DataRoute dr = null;
		Class<? extends Persistable> entityClass = baseManager.getEntityClass();
		if (entityClass == null
				|| (dr = entityClass.getAnnotation(DataRoute.class)) == null)
			return jp.proceed();
		String groupName = dr.value();
		Object obj = eval(groupName, jp, null);
		if (obj == null)
			return jp.proceed();
		else
			groupName = obj.toString();
		if (groupName.equals(DataRouteContext.getName()))
			return jp.proceed();
		DataRouteContext.setName(groupName);
		try {
			return jp.proceed();
		} finally {
			DataRouteContext.setName(null);
		}
	}

}
