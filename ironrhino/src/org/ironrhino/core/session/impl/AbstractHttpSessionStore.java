package org.ironrhino.core.session.impl;

import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.SessionCompressorManager;
import org.ironrhino.core.session.WrappedHttpSession;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractHttpSessionStore implements HttpSessionStore {

	@Autowired
	private SessionCompressorManager sessionCompressorManager;

	public void initialize(WrappedHttpSession session) {
		sessionCompressorManager.uncompress(session, getSessionString(session));
	}

	public void save(WrappedHttpSession session) {
		saveSessionString(session, sessionCompressorManager.compress(session));
	}

	public abstract String getSessionString(WrappedHttpSession session);

	public abstract void saveSessionString(WrappedHttpSession session,
			String sessionString);

}
