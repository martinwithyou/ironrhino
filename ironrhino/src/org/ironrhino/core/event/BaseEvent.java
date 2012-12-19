package org.ironrhino.core.event;

import org.ironrhino.core.util.AppInfo;
import org.springframework.context.ApplicationEvent;

public class BaseEvent<T> extends ApplicationEvent {

	private static final long serialVersionUID = -2892858943541156897L;

	private String instanceId = AppInfo.getInstanceId();

	public BaseEvent(T source) {
		super(source);
	}

	@SuppressWarnings("unchecked")
	public T getSource() {
		return (T) super.getSource();
	}

	public String getInstanceId() {
		return instanceId;
	}

}
