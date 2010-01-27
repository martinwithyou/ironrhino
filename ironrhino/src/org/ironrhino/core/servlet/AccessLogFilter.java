package org.ironrhino.core.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.ums.security.AuthenticationFilter;

public class AccessLogFilter implements Filter {

	private Log log = LogFactory.getLog("access");

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;

		MDC.put("remoteAddr", RequestUtils.getRemoteAddr(request));
		MDC.put("method", request.getMethod());
		StringBuilder url = new StringBuilder().append(request.getRequestURI());
		if (StringUtils.isNotBlank(request.getQueryString()))
			url.append('?').append(request.getQueryString());
		MDC.put("url", url.toString());
		String s = request.getHeader("User-Agent");
		if (s != null)
			MDC.put("userAgent", s);
		s = request.getHeader("Referer");
		if (s != null)
			MDC.put("referer", s);
		s = RequestUtils.getCookieValue(request,
				AuthenticationFilter.COOKIE_NAME_LOGIN_USER);
		if (s != null)
			MDC.put("username", s);
		log.info("");
		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

}
