package org.ironrhino.core.session.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.security.Blowfish;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.session.HttpWrappedSession;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;

public class CookieBasedSessionManager implements HttpSessionManager {

	private Log log = LogFactory.getLog(CookieBasedSessionManager.class);

	public static final String SESSION_COOKIE_PREFIX = "s_";

	public static final int SINGLE_COOKIE_SIZE = 2 * 1024;

	// cookies

	public void initialize(HttpWrappedSession session) {
		Map attrMap = null;
		String value = getCookie(session);
		if (StringUtils.isNotBlank(value)) {
			byte[] bytes = Blowfish.decryptStringToBytes(value);
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bis);
				attrMap = (Map) ois.readObject();
			} catch (Exception e) {
				clearCookie(session);
				log.error(e.getMessage(), e);
			}
		}
		if (attrMap == null)
			attrMap = new HashMap();
		session.setAttrMap(attrMap);
	}

	public void save(HttpWrappedSession session) {
		String referer = session.getHttpContext().getRequest().getHeader(
				"Referer");
		if (referer == null)
			return;
		Map<String, Object> attrMap = session.getAttrMap();
		if (attrMap == null)
			return;
		SecurityContext sc = (SecurityContext) attrMap
				.get(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
		if (sc != null && sc.getAuthentication() != null
				&& !sc.getAuthentication().isAuthenticated())
			attrMap
					.remove(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
		if (attrMap.size() == 0)
			return;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(attrMap);
			oos.close();
			byte[] bytes = bos.toByteArray();
			bos.close();
			String value = Blowfish.encryptBytesToString(bytes);
			saveCookie(session, value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void invalidate(HttpWrappedSession session) {
		session.getAttrMap().clear();
		clearCookie(session);

	}

	private String getCookie(HttpWrappedSession session) {
		Map<String, String> cookieMap = new HashMap<String, String>(3);
		Cookie[] cookies = session.getHttpContext().getRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies)
				if (cookie.getName().startsWith(SESSION_COOKIE_PREFIX)) {
					try {
						cookieMap.put(cookie.getName(), URLDecoder.decode(
								cookie.getValue(), "UTF-8"));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cookieMap.size(); i++) {
			String s = cookieMap.get(SESSION_COOKIE_PREFIX + i);
			if (s == null) {
				log.error(SESSION_COOKIE_PREFIX + i + " is null");
				return null;
			}
			sb.append(s);
		}
		return sb.toString();
	}

	private void saveCookie(HttpWrappedSession session, String value) {
		clearCookie(session);
		if (StringUtils.isNotBlank(value)) {
			int pieces = value.length() / SINGLE_COOKIE_SIZE;
			if (value.length() % SINGLE_COOKIE_SIZE != 0)
				pieces++;
			for (int i = 0; i < pieces; i++)
				RequestUtils
						.saveCookie(
								session.getHttpContext().getRequest(),
								session.getHttpContext().getResponse(),
								SESSION_COOKIE_PREFIX + i,
								value.substring(i * SINGLE_COOKIE_SIZE,
										i == pieces - 1 ? value.length()
												: (i + 1) * SINGLE_COOKIE_SIZE),
								true);
		}
	}

	private void clearCookie(HttpWrappedSession session) {
		Cookie[] cookies = session.getHttpContext().getRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies)
				if (cookie.getName().startsWith(SESSION_COOKIE_PREFIX)) {
					RequestUtils.deleteCookie(session.getHttpContext()
							.getRequest(), session.getHttpContext()
							.getResponse(), cookie.getName(), true);
				}
		}
	}

}
