package org.ironrhino.core.session;

import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.ironrhino.core.security.util.RC4;

public class WrappedHttpServletRequest extends HttpServletRequestWrapper {

	private WrappedHttpSession session;

	public WrappedHttpServletRequest(HttpServletRequest request,
			WrappedHttpSession session) {
		super(request);
		this.session = session;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return session;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return session.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return session.isRequestedSessionIdFromURL();
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

	@Override
	public String getRequestedSessionId() {
		return session.getId();
	}

	@Override
	public String getParameter(String name) {
		String value = super.getParameter(name);
		value = decryptIfNecessary(name, value);
		return value;
	}

	@Override
	public Map getParameterMap() {
		Map<String, String[]> map = super.getParameterMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String name = entry.getKey();
			String[] value = entry.getValue();
			for (int i = 0; i < value.length; i++)
				value[i] = decryptIfNecessary(name, value[i]);
		}
		return map;
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] value = super.getParameterValues(name);
		for (int i = 0; i < value.length; i++)
			value[i] = decryptIfNecessary(name, value[i]);
		return value;
	}

	private String decryptIfNecessary(String name, String value) {
		if (value != null && value.length() > 50
				&& name.toLowerCase().endsWith("password")) {
			for (char c : value.toCharArray())
				if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f'))
					return value;
			String key = session.getSessionTracker();
			try {
				String str = URLDecoder
						.decode(RC4.decrypt(value, key), "UTF-8");
				if (str.endsWith(key))
					value = str.substring(0, str.length() - key.length());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}
}
