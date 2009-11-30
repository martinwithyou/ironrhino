package org.ironrhino.core.session.impl;

import java.math.BigInteger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.WrappedHttpSession;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.NumberUtils;
import org.ironrhino.core.util.RequestUtils;

@Singleton
@Named("httpSessionManager")
public class DefaultHttpSessionManager implements HttpSessionManager {

	protected Log log = LogFactory.getLog(this.getClass());

	private static final String SALT = "awpeqaidasdfaioiaoduifayzuxyaaokadoaifaodiaoi";

	private static final String SESSION_TRACKER_SEPERATOR = "-";

	public static final int DEFAULT_MAXINACTIVEINTERVAL = 1800; // in seconds

	public static final int DEFAULT_MINACTIVEINTERVAL = 60;// in seconds

	@Inject
	@Named("cookieBased")
	private HttpSessionStore cookieBased;

	@Inject
	@Named("cacheBased")
	private HttpSessionStore cacheBased;

	private int maxInactiveInterval = DEFAULT_MAXINACTIVEINTERVAL;

	private int minActiveInterval = DEFAULT_MINACTIVEINTERVAL;

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	@Override
	public int getMinActiveInterval() {
		return minActiveInterval;
	}

	public void setMinActiveInterval(int minActiveInterval) {
		this.minActiveInterval = minActiveInterval;
	}

	@Override
	public void initialize(WrappedHttpSession session) {
		session.setMaxInactiveInterval(getMaxInactiveInterval());
		String sessionTracker = session.getSessionTracker();
		long now = session.getNow();
		String sessionId = null;
		long creationTime = now;
		long lastAccessedTime = now;

		if (StringUtils.isNotBlank(sessionTracker)) {
			sessionTracker = CodecUtils.swap(sessionTracker);
			if (session.isRequestedSessionIdFromURL()) {
				sessionId = sessionTracker;
			} else {
				try {
					String[] array = sessionTracker
							.split(SESSION_TRACKER_SEPERATOR);
					sessionId = array[0];
					if (array.length > 1)
						creationTime = NumberUtils.xToDecimal(62, array[1])
								.longValue();
					if (array.length > 2)
						lastAccessedTime = NumberUtils.xToDecimal(62, array[2])
								.longValue();
					boolean timeout = now - lastAccessedTime > session
							.getMaxInactiveInterval() * 1000;
					if (timeout) {
						invalidate(session);
						return;
					}
				} catch (Exception e) {
					invalidate(session);
					return;
				}
			}
		} else {
			session.setNew(true);
			sessionId = CodecUtils.nextId(SALT);
		}

		session.setId(sessionId);
		session.setCreationTime(creationTime);
		session.setLastAccessedTime(lastAccessedTime);
		doInitialize(session);

	}

	@Override
	public void save(WrappedHttpSession session) {
		boolean sessionTrackerChanged = false;
		if (session.isInvalid()) {
			sessionTrackerChanged = true;
		}
		if (session.isNew())
			sessionTrackerChanged = true;
		if (session.getNow() - session.getLastAccessedTime() > getMinActiveInterval() * 1000) {
			session.setLastAccessedTime(session.getNow());
			sessionTrackerChanged = true;
		}
		if (!session.isRequestedSessionIdFromURL() && sessionTrackerChanged) {
			session.resetSessionTracker();
			RequestUtils.saveCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(),
					WrappedHttpSession.SESSION_TRACKER_NAME, session
							.getSessionTracker(), true);
		}

		if (session.isDirty()) {
			doSave(session);
		}
	}

	@Override
	public void invalidate(WrappedHttpSession session) {
		session.setInvalid(true);
		session.getAttrMap().clear();
		if (!session.isRequestedSessionIdFromURL()) {
			RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(),
					WrappedHttpSession.SESSION_TRACKER_NAME, true);
		}
		doInvalidate(session);
		session.setId(CodecUtils.nextId(SALT));
		session.setCreationTime(session.getNow());
		session.setLastAccessedTime(session.getNow());
		session.resetSessionTracker();
	}

	@Override
	public String getSessionTracker(WrappedHttpSession session) {
		if (session.isRequestedSessionIdFromURL())
			return session.getId();
		StringBuilder sb = new StringBuilder();
		sb.append(session.getId());
		sb.append(SESSION_TRACKER_SEPERATOR);
		sb.append(NumberUtils.decimalToX(62, BigInteger.valueOf(session
				.getCreationTime())));
		sb.append(SESSION_TRACKER_SEPERATOR);
		sb.append(NumberUtils.decimalToX(62, BigInteger.valueOf(session
				.getLastAccessedTime())));
		return CodecUtils.swap(sb.toString());
	}

	private void doInitialize(WrappedHttpSession session) {
		if (session.isRequestedSessionIdFromURL())
			cacheBased.initialize(session);
		else
			cookieBased.initialize(session);
	}

	private void doSave(WrappedHttpSession session) {
		if (session.isRequestedSessionIdFromURL())
			cacheBased.save(session);
		else
			cookieBased.save(session);

	}

	private void doInvalidate(WrappedHttpSession session) {
		if (session.isRequestedSessionIdFromURL())
			cacheBased.invalidate(session);
		else
			cookieBased.invalidate(session);
	}

}
