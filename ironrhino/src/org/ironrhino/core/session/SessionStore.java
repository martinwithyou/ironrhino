package org.ironrhino.core.session;

public interface SessionStore {

	void save(Session session);

	void invalidate(Session session);

	void initialize(Session session);

}
