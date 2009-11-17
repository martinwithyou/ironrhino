package org.ironrhino.core.session.impl;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.session.WrappedHttpSession;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.stereotype.Component;

@Component("cookieBased")
public class CookieBasedHttpSessionStore extends AbstractHttpSessionStore {

	protected Log log = LogFactory.getLog(this.getClass());

	public static final String SESSION_COOKIE_PREFIX = "s_";

	public static final int SINGLE_COOKIE_SIZE = 2 * 1024;

	// cookies

	public String getSessionString(WrappedHttpSession session) {
		return Blowfish.decrypt(getCookie(session));
	}

	public void saveSessionString(WrappedHttpSession session,
			String sessionString) {
		saveCookie(session, Blowfish.encrypt(sessionString));
	}

	public void invalidate(WrappedHttpSession session) {
		clearCookie(session);

	}

	private String getCookie(WrappedHttpSession session) {
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

	private void saveCookie(WrappedHttpSession session, String value) {
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

	private void clearCookie(WrappedHttpSession session) {
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
