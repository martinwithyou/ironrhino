package org.ironrhino.core.cache;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class CacheContext {

	private static Log log = LogFactory.getLog(CacheContext.class);

	public static final String FORCE_FLUSH_PARAM_NAME = "_ff_";

	public static final int DEFAULT_TIME_TO_LIVE = 3600 * 24;

	public static final int DEFAULT_TIME_TO_IDLE = 3600;

	public static final String DEFAULT_SCOPE = "application";

	public static final String PAGE_FRAGMENT_CACHE_NAME = "pageFragmentCache";

	private static ThreadLocal<Boolean> bypass = new ThreadLocal<Boolean>();

	public static void setBypass() {
		bypass.set(true);
	}

	public static boolean isBypass() {
		Boolean b = bypass.get();
		boolean bl = b != null && b.booleanValue();
		bypass.set(false);
		return bl;
	}

	public static boolean forceFlush() {
		try {
			return ServletActionContext.getRequest() != null
					&& ServletActionContext.getRequest().getParameter(
							FORCE_FLUSH_PARAM_NAME) != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static String getPageFragment(String key, String scope) {
		try {
			if (CacheContext.forceFlush())
				return null;
			Element element = getPageFragmentCache().get(
					completeKey(key, scope));
			if (element != null)
				return (String) element.getValue();
			return null;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static void putPageFragment(String key, String content,
			String scope, int timeToLive, int timeToIdle) {
		try {
			Element element = new Element(completeKey(key, scope), content);
			if (timeToIdle > 0)
				element.setTimeToIdle(timeToIdle);
			if (timeToLive > 0)
				element.setTimeToLive(timeToLive);
			getPageFragmentCache().put(element);
		} catch (Throwable e) {
		}
	}

	private static String completeKey(String key, String scope) {
		HttpServletRequest request = ServletActionContext.getRequest();
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		if (scope.equalsIgnoreCase("session"))
			sb.append("," + request.getSession(true).getId());
		return sb.toString();
	}

	private static Cache getPageFragmentCache() {
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
