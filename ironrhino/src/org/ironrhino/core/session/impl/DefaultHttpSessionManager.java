package org.ironrhino.core.session.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpSessionStore;
import org.ironrhino.core.session.WrappedHttpSession;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.NumberUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.Authentication;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("httpSessionManager")
public class DefaultHttpSessionManager implements HttpSessionManager {

	protected Log log = LogFactory.getLog(this.getClass());

	private static final String SALT = "awpeqaidasdfaioiaoduifayzuxyaaokadoaifaodiaoi";

	private static final int SESSION_CREATIONTIME_SCALE = 62;

	private static final int SESSION_LASTACCESSEDTIME_SCALE = 61;

	private static final String SESSION_TRACKER_SEPERATOR = "-";

	public static final int DEFAULT_MAXINACTIVEINTERVAL = 1800; // in seconds

	public static final int DEFAULT_MINACTIVEINTERVAL = 60;// in seconds

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	@Qualifier("cookieBased")
	private HttpSessionStore cookieBased;

	@Autowired
	@Qualifier("cacheBased")
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
		String username = null;

		if (StringUtils.isNotBlank(sessionTracker)) {
			if (session.isRequestedSessionIdFromURL()) {
				sessionId = sessionTracker;
			} else {
				try {
					String[] array = sessionTracker
							.split(SESSION_TRACKER_SEPERATOR);
					sessionId = array[0];
					if (array.length > 1)
						creationTime = NumberUtils.xToDecimal(
								SESSION_CREATIONTIME_SCALE, array[1])
								.longValue();
					if (array.length > 2)
						lastAccessedTime = NumberUtils.xToDecimal(
								SESSION_LASTACCESSEDTIME_SCALE, array[2])
								.longValue();
					if (array.length > 3)
						username = Blowfish.decrypt(array[3]);
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

		if (username != null) {
			UserDetails ud = null;
			try {
				ud = userDetailsService.loadUserByUsername(username);
			} catch (UsernameNotFoundException e) {
				invalidate(session);
				return;
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
			if (ud != null) {
				session.setUsername(username);
				SecurityContext sc = new SecurityContextImpl();
				Authentication auth = new UsernamePasswordAuthenticationToken(
						ud, ud.getPassword(), ud.getAuthorities());
				sc.setAuthentication(auth);
				Map attrMap = session.getAttrMap();
				if (attrMap == null) {
					attrMap = new HashMap<String, Object>();
					session.setAttrMap(attrMap);
				}
				attrMap
						.put(
								HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY,
								sc);
			}
		}
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
		if (!session.isRequestedSessionIdFromURL()) {
			Map attrMap = session.getAttrMap();
			SecurityContext sc = (SecurityContext) attrMap
					.get(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
			if (sc != null) {
				if (session.getUsername() == null) {
					Authentication auth = sc.getAuthentication();
					if (auth != null && auth.isAuthenticated()) {
						session.setUsername(auth.getName());
						sessionTrackerChanged = true;
					}
				}
				attrMap
						.remove(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
			}
			if (sessionTrackerChanged) {
				session.resetSessionTracker();
				RequestUtils.saveCookie(session.getHttpContext().getRequest(),
						session.getHttpContext().getResponse(),
						WrappedHttpSession.SESSION_TRACKER_NAME, session
								.getSessionTracker(), true);
			}
		}

		if (session.isDirty()) {
			doSave(session);
		}
	}

	@Override
	public void invalidate(WrappedHttpSession session) {
		session.setInvalid(true);
		session.setUsername(null);
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
		sb.append(NumberUtils.decimalToX(SESSION_CREATIONTIME_SCALE, BigInteger
				.valueOf(session.getCreationTime())));
		sb.append(SESSION_TRACKER_SEPERATOR);
		sb.append(NumberUtils.decimalToX(SESSION_LASTACCESSEDTIME_SCALE,
				BigInteger.valueOf(session.getLastAccessedTime())));
		if (session.getUsername() != null) {
			sb.append(SESSION_TRACKER_SEPERATOR);
			sb.append(Blowfish.encrypt(session.getUsername()));
		}
		return sb.toString();
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
