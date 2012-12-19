package org.ironrhino.security.event;

import org.ironrhino.security.model.User;

public class LoginEvent extends AbstractEvent {

	private static final long serialVersionUID = -6090070171986100664L;

	public LoginEvent(User user) {
		super(user);
	}

	public LoginEvent(User user, String from, String provider) {
		super(user, from, provider);
	}

}
