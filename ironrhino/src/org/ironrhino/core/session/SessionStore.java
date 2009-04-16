package org.ironrhino.core.session;

public interface SessionStore {

	Object getAttribute(Session session, String key);

	void save(Session session);

	void invalidate(Session session);

}
