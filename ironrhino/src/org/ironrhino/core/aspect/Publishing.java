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
import org.springframework.core.Ordered;

/**
 * Use for record model's CRUD operations
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.annotation.PublishAware
 */
@Aspect
public class Publishing implements Ordered {

	@Autowired
	private EventPublisher eventPublisher;

	private int order;

	@Around("execution(* org.ironrhino..service.*Manager.save*(*)) and args(entity) and @args(org.ironrhino.core.annotation.PublishAware)")
	public void save(ProceedingJoinPoint call, Entity entity) throws Throwable {
		boolean isNew = entity.isNew();
		try {
			call.proceed();
			if (eventPublisher != null)
				eventPublisher.publish(new EntityOperationEvent(entity,
						isNew ? EntityOperationType.CREATE
								: EntityOperationType.UPDATE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterReturning("execution(* org.ironrhino..service.*Manager.delete*(*)) and args(entity) and @args(org.ironrhino.core.annotation.PublishAware)")
	public void delete(Entity entity) {
		if (eventPublisher != null)
			eventPublisher.publish(new EntityOperationEvent(entity,
					EntityOperationType.DELETE));
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
