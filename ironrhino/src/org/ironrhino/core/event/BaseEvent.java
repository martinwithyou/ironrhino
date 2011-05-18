package org.ironrhino.core.event;

import org.ironrhino.core.util.AppInfo;
import org.springframework.context.ApplicationEvent;

public class BaseEvent extends ApplicationEvent {

	private String instanceId = AppInfo.getInstanceId();

	public BaseEvent(Object source) {
		super(source);
	}

	public String getInstanceId() {
		return instanceId;
	}

}
