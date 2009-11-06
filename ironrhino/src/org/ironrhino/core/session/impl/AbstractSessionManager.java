package org.ironrhino.core.session.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.session.Constants;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpWrappedSession;
import org.ironrhino.core.util.RequestUtils;

public abstract class AbstractSessionManager implements HttpSessionManager {

	protected Log log = LogFactory.getLog(this.getClass());

	public static final String KEY_LASTACCESSEDTIME = "__KEY_LASTACCESSEDTIME__";
	public static final String KEY_CREATIONTIME = "__KEY_CREATIONTIME__";

	public void initialize(HttpWrappedSession session) {
		doInitialize(session);
		long now = session.getNow();
		Long creationTime = (Long) session.getAttrMap().get(KEY_CREATIONTIME);
		Long lastAccessedTime = (Long) session.getAttrMap().get(
				KEY_LASTACCESSEDTIME);
		session.getAttrMap().remove(KEY_CREATIONTIME);
		session.getAttrMap().remove(KEY_LASTACCESSEDTIME);
		session.setCreationTime(creationTime != null ? creationTime : now);
		session.setLastAccessedTime(lastAccessedTime != null ? lastAccessedTime
				: now);
		boolean timeout = lastAccessedTime != null
				&& now - lastAccessedTime > session.getMaxInactiveInterval() * 1000;
		if (timeout) 
			invalidate(session);
	}

	public void save(HttpWrappedSession session) {
		if (session.getNow() == session.getLastAccessedTime()
				|| (session.getNow() - session.getLastAccessedTime()) > Constants.SESSION_TOLERATE_INTERVAL * 1000) {
			session.setLastAccessedTime(session.getNow());
			session.setDirty(true);
		}
		if (session.isNew()) {
			RequestUtils.saveCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(),
					Constants.COOKIE_NAME_SESSION_ID, session.getId(), true);
		}
		if (session.isDirty()) {
			session.getAttrMap().put(KEY_LASTACCESSEDTIME,
					session.getLastAccessedTime());
			session.getAttrMap().put(KEY_CREATIONTIME,
					session.getCreationTime());
			doSave(session);
		}
	}

	public void invalidate(HttpWrappedSession session) {
		session.getAttrMap().clear();
		RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
				session.getHttpContext().getResponse(),
				Constants.COOKIE_NAME_SESSION_ID, true);
		RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
				session.getHttpContext().getResponse(),
				Constants.COOKIE_NAME_ENCRYPT_LOGIN_USER, true);
		doInvalidate(session);
	}

	public abstract void doInitialize(HttpWrappedSession session);

	public abstract void doSave(HttpWrappedSession session);

	public abstract void doInvalidate(HttpWrappedSession session);
}
