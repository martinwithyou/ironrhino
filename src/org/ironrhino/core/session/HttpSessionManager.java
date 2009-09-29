package org.ironrhino.core.session;

public interface HttpSessionManager {

	public void save(HttpWrappedSession sesion);

	public void initialize(HttpWrappedSession session);

	public void invalidate(HttpWrappedSession sesion);

}
