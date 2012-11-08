package org.ironrhino.core.event;

import org.ironrhino.core.util.AppInfo;
import org.springframework.context.ApplicationEvent;

public class BaseEvent extends ApplicationEvent {

	private static final long serialVersionUID = -2892858943541156897L;

	private String instanceId = AppInfo.getInstanceId();

	public BaseEvent(Object source) {
		super(source);
	}

	public String getInstanceId() {
		return instanceId;
	}

}
