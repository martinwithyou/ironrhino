package org.ironrhino.core.ext.ehcache;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.web.context.support.WebApplicationContextUtils;

public class CacheTag extends BodyTagSupport {
	public static final String CACHE_NAME = "pageFragmentCache";

	private String actualKey;

	private String key = null;

	private int timeToLive;

	private int timeToIdle;

	private String scope = "application";

	private transient Cache cache = null;

	public void setKey(String key) {
		this.key = key;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setTimeToIdle(int timeToIdle) {
		this.timeToIdle = timeToIdle;
	}

	public int doAfterBody() throws JspTagException {
		String body = null;
		try {
			if ((bodyContent != null)
					&& ((body = bodyContent.getString()) != null)) {
				Element element = new Element(actualKey, body);
				if (timeToIdle > 0)
					element.setTimeToIdle(timeToIdle);
				if (timeToLive > 0)
					element.setTimeToLive(timeToLive);
				cache.put(element);
			}
			if (bodyContent != null) {
				bodyContent.clearBody();
				bodyContent.write(body);
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}
		} catch (java.io.IOException e) {
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return SKIP_BODY;
	}

	public int doStartTag() throws JspTagException {
		if (cache == null)
			cache = getCache();
		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();
		StringBuilder sb = new StringBuilder();
		sb.append(request.getRequestURL());
		sb.append("?");
		sb.append(request.getQueryString());
		sb.append(",");
		sb.append(key);
		if (scope.equalsIgnoreCase("session"))
			sb.append("," + request.getSession(true).getId());
		actualKey = sb.toString();
		Element element = cache.get(actualKey);
		String content = null;
		if (element != null)
			content = (String) element.getValue();
		if ((content != null)) {
			try {
				pageContext.getOut().write(content);
			} catch (IOException e) {
				throw new JspTagException("IO Error: " + e.getMessage());
			}
			return SKIP_BODY;
		}
		return EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspTagException {
		return EVAL_PAGE;
	}

	protected Cache getCache() {
		return (Cache) WebApplicationContextUtils.getWebApplicationContext(
				pageContext.getServletContext()).getBean(CACHE_NAME);
	}

}
