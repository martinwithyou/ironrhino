package org.ironrhino.core.session;

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

public class CookieSessionStore implements SessionStore {

	private Log log = LogFactory.getLog(CookieSessionStore.class);

	public static final String SESSION_COOKIE_PREFIX = "s_";

	public static final int SINGLE_COOKIE_SIZE = 4 * 1024;

	// cookies
	private Map<String, String> cookieMap = new HashMap<String, String>(3);

	public void initialize(Session session) {
		Map attrMap = null;
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
		for (int i = 0; i < cookieMap.size(); i++)
			sb.append(cookieMap.get(SESSION_COOKIE_PREFIX + i));
		String value = sb.toString();
		if (StringUtils.isNotBlank(value)) {
			byte[] bytes = Blowfish.decryptStringToBytes(value);
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bis);
				attrMap = (Map) ois.readObject();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		if (attrMap == null)
			attrMap = new HashMap();
		session.setAttrMap(attrMap);
	}

	public void save(Session session) {
		Map<String, Object> attrMap = session.getAttrMap();
		Map<String, Object> toDump = new HashMap();
		for (Map.Entry<String, Object> entry : attrMap.entrySet())
			if (entry.getValue() != null)
				toDump.put(entry.getKey(), entry.getValue());
		if (toDump.size() == 0)
			RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(),
					SESSION_COOKIE_PREFIX);
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(toDump);
			oos.close();
			byte[] bytes = bos.toByteArray();
			bos.close();
			String value = Blowfish.encryptBytesToString(bytes);
			if (StringUtils.isNotBlank(value)) {
				int pieces = value.length() / SINGLE_COOKIE_SIZE;
				if (value.length() % SINGLE_COOKIE_SIZE != 0)
					pieces++;
				for (int i = 0; i < pieces; i++)
					RequestUtils.saveCookie(session.getHttpContext()
							.getRequest(), session.getHttpContext()
							.getResponse(), SESSION_COOKIE_PREFIX + i, value
							.substring(i * SINGLE_COOKIE_SIZE,
									i == pieces - 1 ? value.length() : (i + 1)
											* SINGLE_COOKIE_SIZE));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void invalidate(Session session) {
		for (int i = 0; i < cookieMap.size(); i++)
			RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(),
					SESSION_COOKIE_PREFIX + i);
	}

}
