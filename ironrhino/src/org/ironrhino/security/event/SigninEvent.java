package org.ironrhino.security.event;

import org.ironrhino.security.model.User;

public class SigninEvent extends AbstractEvent {

	private static final long serialVersionUID = -6090070171986100664L;

	public SigninEvent(User user) {
		super(user);
	}

	public SigninEvent(User user, String from, String provider) {
		super(user, from, provider);
	}

}
