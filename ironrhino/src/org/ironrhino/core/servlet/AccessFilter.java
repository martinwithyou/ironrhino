package org.ironrhino.core.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
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
import org.ironrhino.core.spring.security.DefaultAuthenticationSuccessHandler;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Value;

@Singleton
@Named
public class AccessFilter implements Filter {

	private Log accesLog = LogFactory.getLog("access");

	private Log accesWarnLog = LogFactory.getLog("access-warn");

	public static final long DEFAULT_RESPONSETIMETHRESHOLD = 5000;

	public static final boolean DEFAULT_PRINT = true;

	public static final String DEFAULT_EXCLUDEPATTERNS = "/remoting/*,/assets/*";

	@Value("${accessFilter.responseTimeThreshold:"
			+ DEFAULT_RESPONSETIMETHRESHOLD + "}")
	public static long responseTimeThreshold = DEFAULT_RESPONSETIMETHRESHOLD;

	@Value("${accessFilter.print:" + DEFAULT_PRINT + "}")
	private boolean print = DEFAULT_PRINT;

	@Value("${accessFilter.excludePatterns:" + DEFAULT_EXCLUDEPATTERNS + "}")
	private String excludePatterns = DEFAULT_EXCLUDEPATTERNS;

	private List<String> excludePatternsList = Collections.EMPTY_LIST;

	public void setExcludePatterns(String excludePatterns) {
		this.excludePatterns = excludePatterns;
	}

	public void setPrint(boolean print) {
		this.print = print;
	}

	public static long getResponseTimeThreshold() {
		return responseTimeThreshold;
	}

	public static void setResponseTimeThreshold(long responseTimeThreshold) {
		AccessFilter.responseTimeThreshold = responseTimeThreshold;
	}

	@PostConstruct
	public void _init() {
		excludePatternsList = Arrays.asList(excludePatterns.split(","));
	}

	@Override
	public void init(FilterConfig filterConfig) {
		_init();
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
				DefaultAuthenticationSuccessHandler.COOKIE_NAME_LOGIN_USER);
		if (s != null)
			MDC.put("username", s);
		if (print) {
			boolean excluded = false;
			for (String pattern : excludePatternsList) {
				String path = request.getRequestURI();
				path = path.substring(request.getContextPath().length());
				if (org.ironrhino.core.util.StringUtils.matchesWildcard(path,
						pattern)) {
					excluded = true;
					break;
				}
			}
			if (!excluded)
				accesLog.info("");
		}
		long start = System.currentTimeMillis();
		chain.doFilter(req, resp);
		long responseTime = System.currentTimeMillis() - start;
		if (responseTime > responseTimeThreshold)
			accesWarnLog.warn(" response time:" + responseTime + "ms");
	}

	@Override
	public void destroy() {

	}

}
