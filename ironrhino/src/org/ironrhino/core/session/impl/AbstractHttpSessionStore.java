package org.ironrhino.core.session.impl;

import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.SessionCompressorManager;
import org.ironrhino.core.session.WrappedHttpSession;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractHttpSessionStore implements HttpSessionStore {

	@Autowired
	private SessionCompressorManager sessionCompressorManager;

	public void initialize(WrappedHttpSession session) {
		String sessionString = getSessionString(session);
		sessionCompressorManager.uncompress(session, sessionString);
	}

	public void save(WrappedHttpSession session) {
		String str = sessionCompressorManager.compress(session);
		saveSessionString(session, str);
	}

	public abstract String getSessionString(WrappedHttpSession session);

	public abstract void saveSessionString(WrappedHttpSession session,
			String sessionString);

}
