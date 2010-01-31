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

	public static final String KEY_EXCLUDE_PATTERNS = "excludePatterns";

	public static final String DEFAULT_EXCLUDE_PATTERNS = "/remoting/*";

	public static final String KEY_PRINT = "print";

	private String[] excludePatterns;

	private boolean print = true;

	@Override
	public void init(FilterConfig filterConfig) {
		if ("false".equals(filterConfig.getInitParameter(KEY_PRINT)))
			print = false;
		String str = filterConfig.getInitParameter(KEY_EXCLUDE_PATTERNS);
		if (StringUtils.isBlank(str))
			str = DEFAULT_EXCLUDE_PATTERNS;
		excludePatterns = str.split(",");
	}

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
		boolean excluded = false;
		if (print) {
			for (String pattern : excludePatterns) {
				String path = request.getRequestURI();
				path = path.substring(request.getContextPath().length());
				if (org.ironrhino.core.util.StringUtils.matchesWildcard(path,
						pattern)) {
					excluded = true;
					break;
				}
			}
			if (!excluded)
				log.info("");
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {

	}

}
