package org.ironrhino.core.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;

public class HttpWrappedSession implements Serializable, HttpSession {

	private static final long serialVersionUID = -4227316119138095858L;

	private String id;

	private transient HttpSessionManager sessionManager;

	private transient HttpContext httpContext;

	private Map<String, Object> attrMap = new HashMap<String, Object>();

	private long creationTime;

	private long lastAccessedTime;

	private long now;

	private int maxInactiveInterval;

	private boolean dirty;

	private boolean isnew;

	private boolean fromCookie = true;

	/**
	 * sessionTracker -> id-creationTime-lastAccessedTime-username
	 */
	private String sessionTracker;

	private String username;

	private boolean invalid;

	public HttpWrappedSession(HttpContext context,
			HttpSessionManager sessionManager) {
		now = System.currentTimeMillis();
		this.httpContext = context;
		this.sessionManager = sessionManager;
		sessionTracker = RequestUtils.getCookieValue(context.getRequest(),
				Constants.SESSION_TRACKER_NAME);
		if (StringUtils.isBlank(sessionTracker)) {
			sessionTracker = context.getRequest().getParameter(
					Constants.SESSION_TRACKER_NAME);
			if (StringUtils.isBlank(sessionTracker)) {
				String requestURL = context.getRequest().getRequestURL()
						.toString();
				if (requestURL.indexOf(';') > -1) {
					requestURL = requestURL
							.substring(requestURL.indexOf(';') + 1);
					String[] array = requestURL.split(";");
					for (String pair : array) {
						if (pair.indexOf('=') > 0) {
							String k = pair.substring(0, pair.indexOf('='));
							if (k.equals(Constants.SESSION_TRACKER_NAME)) {
								sessionTracker = pair.substring(pair
										.indexOf('=') + 1);
								break;
							}
						}
					}
				}
			}
			if (StringUtils.isNotBlank(sessionTracker))
				fromCookie = false;
		}

		sessionManager.initialize(this);
	}

	public void save() {
		sessionManager.save(this);
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public Map<String, Object> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map<String, Object> attrMap) {
		this.attrMap = attrMap;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setAttribute(String key, Object object) {
		attrMap.put(key, object);
		if (!HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY
				.equals(key))
			dirty = true;
	}

	@Override
	public Object getAttribute(String key) {
		return attrMap.get(key);
	}

	@Override
	public void removeAttribute(String key) {
		attrMap.remove(key);
		dirty = true;
	}

	@Override
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(attrMap.keySet().iterator());
	}

	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public void invalidate() {
		sessionManager.invalidate(this);
	}

	@Override
	public boolean isNew() {
		return isnew;
	}

	public void setNew(boolean isnew) {
		this.isnew = isnew;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return httpContext.getContext();
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		maxInactiveInterval = arg0;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public long getNow() {
		return now;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public String getSessionTracker() {
		return sessionTracker;
	}

	public void setSessionTracker(String sessionTracker) {
		this.sessionTracker = sessionTracker;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return fromCookie;
	}

	public boolean isRequestedSessionIdFromURL() {
		return !fromCookie;
	}

	@Deprecated
	@Override
	public String[] getValueNames() {
		List names = new ArrayList();

		for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
			names.add(e.nextElement());
		}

		return (String[]) names.toArray(new String[names.size()]);
	}

	@Deprecated
	@Override
	public Object getValue(String key) {
		return getAttribute(key);
	}

	@Deprecated
	@Override
	public void removeValue(String key) {
		removeAttribute(key);
	}

	@Deprecated
	@Override
	public void putValue(String key, Object object) {
		setAttribute(key, object);
	}

	@Deprecated
	@Override
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException(
				"No longer supported method: getSessionContext");
	}

}
