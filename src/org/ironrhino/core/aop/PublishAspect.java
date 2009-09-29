package org.ironrhino.core.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.event.EventPublisher;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.model.Persistable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Use for record model's CRUD operations
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.metadata.PublishAware
 */
@Aspect
@Component
public class PublishAspect extends BaseAspect {

	public PublishAspect() {
		order = -1;
	}

	@Autowired
	private EventPublisher eventPublisher;

	@Around("execution(* org.ironrhino..service.*Manager.save*(*)) and args(entity) and @args(publishAware)")
	public Object save(ProceedingJoinPoint call, Persistable entity,
			PublishAware publishAware) throws Throwable {
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

	@AfterReturning("execution(* org.ironrhino..service.*Manager.delete*(*)) and args(entity) and @args(publishAware)")
	public void delete(Persistable entity, PublishAware publishAware) {
		if (isBypass())
			return;
		if (eventPublisher != null)
			eventPublisher.publish(new EntityOperationEvent(entity,
					EntityOperationType.DELETE));
	}

}
