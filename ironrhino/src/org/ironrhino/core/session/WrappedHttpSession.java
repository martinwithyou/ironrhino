package org.ironrhino.core.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;

public class WrappedHttpSession implements Serializable, HttpSession {

	private static final long serialVersionUID = -4227316119138095858L;

	public static final String SESSION_TRACKER_NAME = "T";

	public static Pattern SESSION_TRACKER_PATTERN = Pattern.compile(';'
			+ SESSION_TRACKER_NAME + "=.+");

	private String id;

	private transient HttpSessionManager httpSessionManager;

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
	 * sessionTracker -> id-creationTime-lastAccessedTime
	 */
	private String sessionTracker;

	private boolean invalid;

	public WrappedHttpSession(HttpContext context,
			HttpSessionManager httpSessionManager) {
		now = System.currentTimeMillis();
		this.httpContext = context;
		this.httpSessionManager = httpSessionManager;
		sessionTracker = RequestUtils.getCookieValue(context.getRequest(),
				SESSION_TRACKER_NAME);
		if (StringUtils.isBlank(sessionTracker)) {
			sessionTracker = context.getRequest().getParameter(
					SESSION_TRACKER_NAME);
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
							if (k.equals(SESSION_TRACKER_NAME)) {
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

		httpSessionManager.initialize(this);
	}

	public void save() {
		httpSessionManager.save(this);
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
		if (isRequestedSessionIdFromURL()
				|| !HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY
						.equals(key)) {
			dirty = true;
		}
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
		httpSessionManager.invalidate(this);
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

	public void resetSessionTracker() {
		this.sessionTracker = httpSessionManager.getSessionTracker(this);
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

	public boolean isRequestedSessionIdFromCookie() {
		return fromCookie;
	}

	public boolean isRequestedSessionIdFromURL() {
		return !fromCookie;
	}

	public String encodeURL(String url) {
		if (!isRequestedSessionIdFromURL() || StringUtils.isBlank(url))
			return url;
		Matcher m = SESSION_TRACKER_PATTERN.matcher(url);
		if (m.find())
			return url;
		return doEncodeURL(url);
	}

	public String encodeRedirectURL(String url) {
		if (!isRequestedSessionIdFromURL() || StringUtils.isBlank(url))
			return url;
		Matcher m = SESSION_TRACKER_PATTERN.matcher(url);
		url = m.replaceAll("");
		return doEncodeURL(url);
	}

	private String doEncodeURL(String url) {
		String[] array = url.split("\\?", 2);
		StringBuilder sb = new StringBuilder();
		sb.append(array[0]);
		if (sessionTracker != null) {
			sb.append(';');
			sb.append(SESSION_TRACKER_NAME);
			sb.append('=');
			sb.append(sessionTracker);
		}
		if (array.length == 2) {
			sb.append('?');
			sb.append(array[1]);
		}
		return sb.toString();
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
