package org.ironrhino.core.session;

import java.io.Serializable;
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
import org.ironrhino.core.util.RequestUtils;

public class HttpWrappedSession implements Serializable, HttpSession {

	private static final long serialVersionUID = -4227316119138095858L;

	private static final String salt = "awpeqaidasdfaioiaoduifayzuxyaaokadoaifaodiaoi";

	private String sessionId;

	private transient HttpSessionManager sessionStoreManager;

	private transient HttpContext httpContext;

	private Map<String, Object> attrMap = new HashMap<String, Object>();

	private long createTime;

	private int maxInactiveInterval = Constants.SESSION_TIMEOUT;

	private boolean darty;

	private boolean isnew;

	private HttpSession target;

	public HttpWrappedSession(HttpContext context,
			HttpSessionManager sessionStoreManager, String sid) {
		this.target = context.getRequest().getSession(false);
		this.httpContext = context;
		this.sessionStoreManager = sessionStoreManager;
		this.sessionId = sid;
		if (StringUtils.isBlank(sessionId)) {
			isnew = true;
			createTime = System.currentTimeMillis();
			sessionId = DigestUtils.md5Hex(salt + UUID.randomUUID().toString());
		}
		sessionStoreManager.initialize(this);
	}

	public void save() {
		if (darty)
			sessionStoreManager.save(this);
		if (isnew)
			RequestUtils.saveCookie(httpContext.getRequest(), httpContext
					.getResponse(), Constants.COOKIE_NAME_SESSION_ID, getId(),
					true);
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

	public String getId() {
		return sessionId;
	}

	public void setAttribute(String key, Object object) {
		attrMap.put(key, object);
		darty = true;
	}

	public Object getAttribute(String key) {
		return attrMap.get(key);
	}

	public void removeAttribute(String key) {
		attrMap.remove(key);
		darty = true;
	}

	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(attrMap.keySet().iterator());
	}

	public long getCreationTime() {
		if (target != null)
			return target.getCreationTime();
		return this.createTime;
	}

	public void invalidate() {
		if (target != null)
			target.invalidate();
		sessionStoreManager.invalidate(this);
	}

	public boolean isNew() {
		if (target != null)
			return target.isNew();
		return isnew;
	}

	public long getLastAccessedTime() {
		if (target != null)
			return target.getLastAccessedTime();
		return createTime;
	}

	public ServletContext getServletContext() {
		return httpContext.getContext();
	}

	public void setMaxInactiveInterval(int arg0) {
		if (target != null)
			target.setMaxInactiveInterval(arg0);
		maxInactiveInterval = arg0;
	}

	public int getMaxInactiveInterval() {
		if (target != null)
			return target.getMaxInactiveInterval();
		return maxInactiveInterval;
	}

	@Deprecated
	public String[] getValueNames() {
		List names = new ArrayList();

		for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
			names.add(e.nextElement());
		}

		return (String[]) names.toArray(new String[names.size()]);
	}

	@Deprecated
	public Object getValue(String key) {
		return getAttribute(key);
	}

	@Deprecated
	public void removeValue(String key) {
		removeAttribute(key);
	}

	@Deprecated
	public void putValue(String key, Object object) {
		setAttribute(key, object);
	}

	@Deprecated
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException(
				"No longer supported method: getSessionContext");
	}

}
