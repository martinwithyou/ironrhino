package org.ironrhino.security.event;

import org.ironrhino.core.event.BaseEvent;
import org.ironrhino.security.model.User;

public class LogoutEvent extends BaseEvent<User> {

	private static final long serialVersionUID = -6090070171986100664L;

	public LogoutEvent(User user) {
		super(user);
	}

	public User getUser() {
		return getSource();
	}

}
