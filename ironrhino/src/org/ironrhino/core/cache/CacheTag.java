package org.ironrhino.core.cache;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.struts2.ServletActionContext;

public class CacheTag extends BodyTagSupport {

	private String key = null;

	private int timeToLive = PageFragmentCacheHelper.DEFAULT_TIME_TO_LIVE;

	private int timeToIdle = PageFragmentCacheHelper.DEFAULT_TIME_TO_IDLE;

	private String scope = PageFragmentCacheHelper.DEFAULT_SCOPE;

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
				PageFragmentCacheHelper.put(key, body, scope, timeToLive,
						timeToIdle);
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
		if (ServletActionContext.getRequest() == null)
			ServletActionContext.setRequest((HttpServletRequest) pageContext
					.getRequest());
		if (ServletActionContext.getResponse() == null)
			ServletActionContext.setResponse((HttpServletResponse) pageContext
					.getResponse());
		if (ServletActionContext.getServletContext() == null)
			ServletActionContext.setServletContext(pageContext
					.getServletContext());
		String content = PageFragmentCacheHelper.get(key, scope);
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

}
