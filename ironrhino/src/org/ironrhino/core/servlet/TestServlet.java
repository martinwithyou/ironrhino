package org.ironrhino.core.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.ironrhino.core.util.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void init() {
		String url = getInitParameter("url");
		if (StringUtils.isBlank(url)) {
			String contextPath = getServletContext().getContextPath();
			url = "http://localhost"
					+ (contextPath.indexOf('/') == 0 ? "" : "/") + contextPath;
		}
		final String testurl = url;
		new Thread() {
			public void run() {
				logger.info("testing: " + testurl);
				HttpRequestBase httpRequest = new HttpGet(testurl);
				try {
					HttpResponse response = HttpClientUtils
							.getDefaultInstance().execute(httpRequest);
					if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK)
						logger.warn("test failed,no response,please check it");
					else
						logger.info("test succussful");
				} catch (Exception e) {
					httpRequest.abort();
					logger.error("test failed", e);
				}

			}
		}.start();
	}

}
