package org.ironrhino.core.session;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ho.yaml.Yaml;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.common.util.SecurityUtils;

public class CookieSessionStore implements SessionStore {

	private Log log = LogFactory.getLog(CookieSessionStore.class);

	private String encryptkey = "alkdfjalkgjalgjalkgadklfajdkfald";

	private String cookieName = "_s_";

	public boolean restored;

	public Object getAttribute(Session session, String key) {
		Map attrMap = session.getAttrMap();
		if (!restored) {
			restored = true;
			RequestUtils.getCookieValue(session.getHttpContext().getRequest(),
					cookieName);
			String value = RequestUtils.getCookieValue(session.getHttpContext()
					.getRequest(), cookieName);
			if (StringUtils.isNotBlank(value)) {
				value = SecurityUtils.decrypt(encryptkey, value);
				try {
					attrMap = (Map) Yaml.load(value);
					session.setAttrMap(attrMap);
				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
					invalidate(session);
				}
			}
		}
		return attrMap.get(key);
	}

	public void save(Session session) {
		// TODO some object like webflow.conversation can't be dump,and some
		// attribute losed
		Map attrMap = session.getAttrMap();
		Map toDump = new HashMap();
		for (Object key : attrMap.keySet())
			if (attrMap.get(key) != null)
				toDump.put(key, attrMap.get(key));
		if (toDump.size() == 0)
			RequestUtils.deleteCookie(session.getHttpContext().getRequest(),
					session.getHttpContext().getResponse(), cookieName);
		if (session.getChangeMark() != null) {
			String value = null;
			try {
				value = Yaml.dump(toDump);
				if (value.length() > 4 * 1024)
					throw new RuntimeException("cookie length > 4k");
				if (StringUtils.isNotBlank(value)) {
					value = SecurityUtils.encrypt(encryptkey, value);
					RequestUtils.saveCookie(session.getHttpContext()
							.getRequest(), session.getHttpContext()
							.getResponse(), cookieName, value);
				}
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void invalidate(Session session) {
		session.getAttrMap().clear();
	}
}
