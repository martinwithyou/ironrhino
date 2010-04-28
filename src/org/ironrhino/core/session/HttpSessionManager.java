package org.ironrhino.core.session;

public interface HttpSessionManager extends HttpSessionStore {

	public String getSessionTracker(WrappedHttpSession session);

	public String getSessionTrackerName();

}
