package org.ironrhino.core.event;

import org.ironrhino.core.util.AppInfo;

public class InstanceLifecycleEvent extends BaseEvent<String> {

	private static final long serialVersionUID = -4318285891244692446L;

	public InstanceLifecycleEvent() {
		super(AppInfo.getHostAddress());
	}

}
