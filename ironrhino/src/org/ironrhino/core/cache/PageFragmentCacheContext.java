package org.ironrhino.core.cache;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PageFragmentCacheContext {

	private static Log log = LogFactory.getLog(PageFragmentCacheContext.class);

	public static final int DEFAULT_TIME_TO_LIVE = 3600 * 24;

	public static final int DEFAULT_TIME_TO_IDLE = 3600;

	public static final String DEFAULT_SCOPE = "application";

	public static final String PAGE_FRAGMENT_CACHE_NAME = "pageFragmentCache";

	public static String get(String key, String scope) {
		try {
			if (CacheContext.forceFlush())
				return null;
			Element element = getCache().get(completeKey(key, scope));
			if (element != null)
				return (String) element.getValue();
			return null;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static void put(String key, String content, String scope,
			int timeToLive, int timeToIdle) {
		try {
			Element element = new Element(completeKey(key, scope), content);
			if (timeToIdle > 0)
				element.setTimeToIdle(timeToIdle);
			if (timeToLive > 0)
				element.setTimeToLive(timeToLive);
			getCache().put(element);
		} catch (Throwable e) {
		}
	}

	private static String completeKey(String key, String scope) {
		HttpServletRequest request = ServletActionContext.getRequest();
		StringBuilder sb = new StringBuilder();
		sb.append(request.getRequestURL());
		sb.append(",");
		sb.append(key);
		if (scope.equalsIgnoreCase("session"))
			sb.append("," + request.getSession(true).getId());
		return sb.toString();
	}

	private static Cache getCache() {
		try {
			return (Cache) WebApplicationContextUtils.getWebApplicationContext(
					ServletActionContext.getServletContext()).getBean(
					PAGE_FRAGMENT_CACHE_NAME);
		} catch (Throwable e) {
			log.error("get cache error:" + e.getMessage(), e);
			return null;
		}
	}

}
