package org.ironrhino.core.event;

import org.ironrhino.core.model.Persistable;
import org.springframework.context.ApplicationEvent;

public class EntityOperationEvent extends ApplicationEvent {

	private static final long serialVersionUID = -3336231774669978161L;

	private EntityOperationType type;

	public EntityOperationEvent(Persistable entity, EntityOperationType type) {
		super(entity);
		this.type = type;
	}

	public Persistable getEntity() {
		return (Persistable) getSource();
	}

	public EntityOperationType getType() {
		return type;
	}

}
