package org.ironrhino.security.event;

import org.ironrhino.core.event.BaseEvent;
import org.ironrhino.security.model.User;

public abstract class AbstractEvent extends BaseEvent<User> {

	private static final long serialVersionUID = 2656926225727363987L;

	private String from; // oauth

	private String provider; // google github

	public AbstractEvent(User user) {
		super(user);
	}

	public AbstractEvent(User user, String from, String provider) {
		super(user);
		this.from = from;
		this.provider = provider;
	}

	public User getUser() {
		return getSource();
	}

	public String getFrom() {
		return from;
	}

	public String getProvider() {
		return provider;
	}

}
