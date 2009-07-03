package org.ironrhino.core.cache;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PageFragmentCacheHelper {

	private static Log log = LogFactory.getLog(PageFragmentCacheHelper.class);

	public static final String CACHE_NAME = "pageFragmentCache";

	public static final String FORCE_FLUSH_PARAM_NAME = "_ff_";

	public static final int DEFAULT_TIME_TO_LIVE = 3600 * 24;

	public static final int DEFAULT_TIME_TO_IDLE = 3600;

	public static final String DEFAULT_SCOPE = "application";

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

	public static String get(String key, String scope) {
		try {
			if (ServletActionContext.getRequest().getParameter(
					FORCE_FLUSH_PARAM_NAME) != null)
				return null;
			Element element = getCache().get(completeKey(key, scope));
			return (String) element.getValue();
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

	public static Cache getCache() {
		try {
			return (Cache) WebApplicationContextUtils.getWebApplicationContext(
					ServletActionContext.getServletContext()).getBean(
					CACHE_NAME);
		} catch (Throwable e) {
			log.error("get cache error:" + e.getMessage(), e);
			return null;
		}
	}

}
