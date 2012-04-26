package org.ironrhino.core.event;

import org.ironrhino.core.model.Persistable;

public class EntityOperationEvent extends BaseEvent {

	private static final long serialVersionUID = -3336231774669978161L;

	private Persistable<?> entity;

	private EntityOperationType type;

	public EntityOperationEvent(Persistable<?> entity, EntityOperationType type) {
		super(entity);
		this.entity = entity;
		this.type = type;
	}

	public Persistable<?> getEntity() {
		return entity;
	}

	public Object getSource() {
		return getEntity();
	}

	public EntityOperationType getType() {
		return type;
	}

}
