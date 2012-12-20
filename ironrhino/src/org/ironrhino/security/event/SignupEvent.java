package org.ironrhino.security.event;

import org.ironrhino.security.model.User;

public class SignupEvent extends AbstractEvent {

	private static final long serialVersionUID = -6090070171986100664L;

	public SignupEvent(User user) {
		super(user);
	}

	public SignupEvent(User user, String from, String provider) {
		super(user, from, provider);
	}

}
