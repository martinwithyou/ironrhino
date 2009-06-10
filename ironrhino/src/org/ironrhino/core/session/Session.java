package org.ironrhino.core.session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;

public class Session implements HttpSession {

	private String sessionId;

	SessionManager sessionManager;

	private HttpContext httpContext;

	private Map attrMap = new HashMap();

	private long createTime;

	private int maxInactiveInterval = 1800;

	private static String SESSION_ID = "_sid_";

	public Session(HttpContext context, SessionManager sessionManager) {
		this.httpContext = context;
		this.sessionManager = sessionManager;
		this.sessionManager.setHttpSession(this);
		sessionManager.initialize();
		createTime = System.currentTimeMillis();
		sessionId = (String) getAttribute(SESSION_ID);
		if (StringUtils.isBlank(sessionId)) {
			sessionId = DigestUtils.md5Hex(UUID.randomUUID().toString());
			setAttribute(SESSION_ID, sessionId);
		}
	}

	public long getCreationTime() {
		return this.createTime;
	}

	public String getId() {
		return sessionId;
	}

	public long getLastAccessedTime() {
		return createTime;
	}

	public ServletContext getServletContext() {
		return httpContext.getContext();
	}

	public void setMaxInactiveInterval(int arg0) {
		maxInactiveInterval = arg0;
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public Object getValue(String key) {
		return getAttribute(key);
	}

	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(attrMap.keySet().iterator());
	}

	public String[] getValueNames() {
		List names = new ArrayList();

		for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
			names.add(e.nextElement());
		}

		return (String[]) names.toArray(new String[names.size()]);
	}

	public void setAttribute(String key, Object object) {
		attrMap.put(key, object);
	}

	public void putValue(String key, Object object) {
		setAttribute(key, object);
	}

	public void removeAttribute(String key) {
		attrMap.remove(key);
	}

	public void removeValue(String key) {
		removeAttribute(key);
	}

	public void invalidate() {
		sessionManager.invalidate();
	}

	public boolean isNew() {
		return true;
	}

	public Object getAttribute(String key) {
		return attrMap.get(key);
	}

	@Deprecated
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException(
				"No longer supported method: getSessionContext");
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public Map getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map attrMap) {
		this.attrMap = attrMap;
	}

	public void save() {
		sessionManager.save();
	}
}
