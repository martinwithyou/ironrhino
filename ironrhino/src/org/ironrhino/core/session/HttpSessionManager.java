package org.ironrhino.core.session;

public interface HttpSessionManager extends HttpSessionStore {

	public String getSessionTracker(WrappedHttpSession session);

	public int getMaxInactiveInterval(); // in seconds

	public int getMinActiveInterval(); // in seconds

}
