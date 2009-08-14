package org.ironrhino.core.session;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;

public class Session implements Serializable, HttpSession {

	private static final String salt = "awpeqaidasdfaioiaoduifayzuxyaaokadoaifaodiaoi";

	public static final String SESSION_ID = "sid";

	public static final int SESSION_TIMEOUT = 1800;

	private String sessionId;

	private transient SessionManager sessionManager;

	private transient HttpContext httpContext;

	private Map<String, Object> attrMap = new HashMap<String, Object>();

	private long createTime;

	private int maxInactiveInterval = 1800;

	public Session(HttpContext context, SessionManager sessionManager,
			String sid) {
		this.httpContext = context;
		this.sessionManager = sessionManager;
		this.sessionId = sid;
		if (StringUtils.isBlank(sessionId)) {
			createTime = System.currentTimeMillis();
			sessionId = DigestUtils.md5Hex(salt + UUID.randomUUID().toString());
		}
		sessionManager.initialize(this);
	}

	public void save() {
		sessionManager.save(this);
		Cookie cookie = new Cookie(SESSION_ID, getId());
		cookie.setMaxAge(getMaxInactiveInterval());
		try {
			String[] array = new URL(httpContext.getRequest().getRequestURL()
					.toString()).getHost().split("\\.");
			StringBuilder domain = new StringBuilder();
			domain.append('.');
			domain.append(array[array.length - 2]);
			domain.append('.');
			domain.append(array[array.length - 1]);
			cookie.setDomain(domain.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		httpContext.getResponse().addCookie(cookie);
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
		sessionManager.invalidate(this);
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

	public Map<String, Object> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map<String, Object> attrMap) {
		this.attrMap = attrMap;
	}

}
