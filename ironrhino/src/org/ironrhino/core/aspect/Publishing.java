package org.ironrhino.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.event.EventPublisher;
import org.ironrhino.core.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Use for record model's CRUD operations
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.annotation.PublishAware
 */
@Aspect
public class Publishing extends BaseAspect {

	@Autowired
	private EventPublisher eventPublisher;

	@Around("execution(* org.ironrhino..service.*Manager.save*(*)) and args(entity) and @args(org.ironrhino.core.annotation.PublishAware)")
	public Object save(ProceedingJoinPoint call, Entity entity)
			throws Throwable {
		if (isBypass())
			return call.proceed();
		boolean isNew = entity.isNew();
		Object result = call.proceed();
		if (eventPublisher != null)
			eventPublisher.publish(new EntityOperationEvent(entity,
					isNew ? EntityOperationType.CREATE
							: EntityOperationType.UPDATE));
		return result;
	}

	@AfterReturning("execution(* org.ironrhino..service.*Manager.delete*(*)) and args(entity) and @args(org.ironrhino.core.annotation.PublishAware)")
	public void delete(Entity entity) {
		if (isBypass())
			return;
		if (eventPublisher != null)
			eventPublisher.publish(new EntityOperationEvent(entity,
					EntityOperationType.DELETE));
	}

}
