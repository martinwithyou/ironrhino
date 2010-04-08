package org.ironrhino.core.struts;

import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;

public class FeedResult implements Result {

	private static final long serialVersionUID = -488409829847617499L;

	private String feedName = "feed";

	private String feedType;

	@Inject(StrutsConstants.STRUTS_I18N_ENCODING)
	private String defaultEncoding;

	protected String getEncoding() {
		String encoding = defaultEncoding;
		if (encoding == null)
			encoding = "UTF-8";
		return encoding;
	}

	public void execute(ActionInvocation ai) throws Exception {
		ServletActionContext.getResponse().setContentType(
				"text/atom+xml;charset=" + getEncoding());
		SyndFeed feed = (SyndFeed) ai.getStack().findValue(feedName);
		if (feed != null) {
			feed.setEncoding(getEncoding());
			feedType = conditionalParse(feedType, ai);
			if (StringUtils.isBlank(feedType) || "atom".equals(feedType))
				feedType = "atom_1.0";
			else if ("rss".equals(feedType))
				feedType = "rss_2.0";
			feed.setFeedType(feedType);
			SyndFeedOutput output = new SyndFeedOutput();
			Writer out = null;
			try {
				out = ServletActionContext.getResponse().getWriter();
				output.output(feed, out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null)
					out.close();
			}
		}
	}

	public void setFeedName(String feedName) {
		this.feedName = feedName;
	}

	public void setFeedType(String feedType) {
		this.feedType = feedType;
	}

	protected String conditionalParse(String param, ActionInvocation invocation) {
		// see org.apache.struts2.dispatcher.StrutsResultSupport
		if (param != null && invocation != null) {
			return TextParseUtil.translateVariables(param, invocation
					.getStack(), new TextParseUtil.ParsedValueEvaluator() {
				public Object evaluate(Object parsedValue) {
					return parsedValue;
				}
			});
		} else {
			return param;
		}
	}
}
