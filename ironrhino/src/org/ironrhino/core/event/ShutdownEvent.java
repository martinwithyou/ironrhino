package org.ironrhino.core.event;

import org.ironrhino.core.util.AppInfo;

public class ShutdownEvent extends BaseEvent<String> {

	private static final long serialVersionUID = 6870119566152595698L;

	public ShutdownEvent() {
		super(AppInfo.getHostAddress());
	}

	public String getHost() {
		return getSource();
	}

}
