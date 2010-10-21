package org.ironrhino.core.session.impl;

import java.math.BigInteger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.WrappedHttpSession;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.NumberUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Value;

@Singleton
@Named("httpSessionManager")
public class DefaultHttpSessionManager implements HttpSessionManager {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String DEFAULT_SESSION_TRACKER_NAME = "T";

	private static final String SALT = "awpeqaidasdfaioiaoduifayzuxyaaokadoaifaodiaoi";

	private static final String SESSION_TRACKER_SEPERATOR = "-";

	public static final int DEFAULT_LIFETIME = -1; // in seconds

	public static final int DEFAULT_MAXINACTIVEINTERVAL = 1800; // in seconds

	public static final int DEFAULT_MINACTIVEINTERVAL = 60;// in seconds

	private String sessionTrackerName = DEFAULT_SESSION_TRACKER_NAME;

	@Inject
	@Named("cookieBased")
	private HttpSessionStore cookieBased;

	@Inject
	@Named("cacheBased")
	private HttpSessionStore cacheBased;

	@Value("${httpSessionManager.lifetime:" + DEFAULT_LIFETIME + "}")
	private int lifetime;

	@Value("${httpSessionManager.maxInactiveInterval:"
			+ DEFAULT_MAXINACTIVEINTERVAL + "}")
	private int maxInactiveInterval;

	@Value("${httpSessionManager.minActiveInterval:"
			+ DEFAULT_MINACTIVEINTERVAL + "}")
	private int minActiveInterval;

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public void setMinActiveInterval(int minActiveInterval) {
		this.minActiveInterval = minActiveInterval;
	}

	public void setSessionTrackerName(String sessionTrackerName) {
		this.sessionTrackerName = sessionTrackerName;
	}

	public String getSessionTrackerName() {
		return sessionTrackerName;
	}

	public void initialize(WrappedHttpSession session) {
	
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
					boolean timeout = (lifetime > 0 && (now - creationTime > lifetime * 1000))
							|| (now - lastAccessedTime > maxInactiveInterval * 1000);
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
		session.setMaxInactiveInterval(maxInactiveInterval);
		session.setMinActiveInterval(minActiveInterval);
		if (session.getSessionTracker() == null)
			session.setSessionTracker(getSessionTracker(session));
		doInitialize(session);

	}

	public void save(WrappedHttpSession session) {
		boolean sessionTrackerChanged = false;
		if (session.isInvalid()) {
			sessionTrackerChanged = true;
		}
		if (session.isNew())
			sessionTrackerChanged = true;
		if (session.getNow() - session.getLastAccessedTime() > session
				.getMinActiveInterval() * 1000) {
			session.setLastAccessedTime(session.getNow());
			sessionTrackerChanged = true;
		}
		// if (!session.isRequestedSessionIdFromURL() && sessionTrackerChanged)
		// {
		if (sessionTrackerChanged) {
			session.resetSessionTracker();
			RequestUtils.saveCookie(session.getRequest(),
					session.getResponse(), getSessionTrackerName(), session
							.getSessionTracker(), true);
		}
		doSave(session);
	}

	public void invalidate(WrappedHttpSession session) {
		session.setInvalid(true);
		session.getAttrMap().clear();
		if (!session.isRequestedSessionIdFromURL()) {
			RequestUtils.deleteCookie(session.getRequest(), session
					.getResponse(), getSessionTrackerName(), true);
		}
		doInvalidate(session);
		session.setId(CodecUtils.nextId(SALT));
		session.setCreationTime(session.getNow());
		session.setLastAccessedTime(session.getNow());
		session.resetSessionTracker();
	}

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
