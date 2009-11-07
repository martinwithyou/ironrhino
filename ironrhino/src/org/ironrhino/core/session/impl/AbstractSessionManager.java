package org.ironrhino.core.session.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.session.Constants;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpWrappedSession;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.NumberUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

public abstract class AbstractSessionManager implements HttpSessionManager {

	protected Log log = LogFactory.getLog(this.getClass());

	private static final String salt = "awpeqaidasdfaioiaoduifayzuxyaaokadoaifaodiaoi";

	@Autowired
	private UserDetailsService userDetailsService;

	private int maxInactiveInterval = Constants.DEFAULT_MAXINACTIVEINTERVAL;

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public void initialize(HttpWrappedSession session) {
		session.setMaxInactiveInterval(getMaxInactiveInterval());
		String sessionTracker = session.getSessionTracker();
		long now = session.getNow();
		String sessionId = null;
		long creationTime = now;
		long lastAccessedTime = now;
		String username = null;
		if (StringUtils.isNotBlank(sessionTracker)) {
			try {
				String[] array = sessionTracker.split("-");
				sessionId = array[0];
				if (array.length > 1)
					creationTime = NumberUtils.xToDecimal(62, array[1])
							.longValue();
				if (array.length > 2)
					lastAccessedTime = NumberUtils.xToDecimal(61, array[2])
							.longValue();
				if (array.length > 3)
					username = Blowfish.decrypt(array[3]);
			} catch (Exception e) {
				invalidate(session);
				return;
			}
		} else {
			session.setNew(true);
			sessionId = CodecUtils.nextId(salt);
		}
		boolean timeout = now - lastAccessedTime > session
				.getMaxInactiveInterval() * 1000;
		if (timeout) {
			invalidate(session);
			return;
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

	public void save(HttpWrappedSession session) {
		boolean sessionTrackerChanged = false;
		if (session.isInvalid()) {
			session.setId(CodecUtils.nextId(salt));
			sessionTrackerChanged = true;
		}
		if (session.isNew())
			sessionTrackerChanged = true;
		if (session.getNow() - session.getLastAccessedTime() > Constants.SESSION_TOLERATE_INTERVAL * 1000) {
			session.setLastAccessedTime(session.getNow());
			sessionTrackerChanged = true;
		}
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
			StringBuilder sb = new StringBuilder();
			sb.append(session.getId());
			sb.append('-');
			sb.append(NumberUtils.decimalToX(62, BigInteger.valueOf(session
					.getCreationTime())));
			sb.append('-');
			sb.append(NumberUtils.decimalToX(61, BigInteger.valueOf(session
					.getLastAccessedTime())));
			if (session.getUsername() != null) {
				sb.append('-');
				sb.append(Blowfish.encrypt(session.getUsername()));
			}
			String sessionTracker = sb.toString();
			session.setSessionTracker(sessionTracker);
			if (session.isRequestedSessionIdFromCookie())
				RequestUtils.saveCookie(session.getHttpContext().getRequest(),
						session.getHttpContext().getResponse(),
						Constants.SESSION_TRACKER_NAME, sessionTracker, true);
		}
		if (session.isDirty()) {
			doSave(session);
		}
	}

	public void invalidate(HttpWrappedSession session) {
		session.setInvalid(true);
		session.getAttrMap().clear();
		if (session.isRequestedSessionIdFromCookie())
			RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(),
					Constants.SESSION_TRACKER_NAME, true);
		doInvalidate(session);
	}

	public abstract void doInitialize(HttpWrappedSession session);

	public abstract void doSave(HttpWrappedSession session);

	public abstract void doInvalidate(HttpWrappedSession session);

}
