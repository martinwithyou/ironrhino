package org.ironrhino.core.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.ironrhino.core.spring.security.DefaultAuthenticationSuccessHandler;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.core.util.UserAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

@Singleton
@Named
public class AccessFilter implements Filter {

	private Logger accesLog = LoggerFactory.getLogger("access");

	private Logger accesWarnLog = LoggerFactory.getLogger("access-warn");

	public static final long DEFAULT_RESPONSETIMETHRESHOLD = 5000;

	public static final boolean DEFAULT_PRINT = true;

	public static final String DEFAULT_EXCLUDEPATTERNS = "/remoting/*,/assets/*";

	@Value("${accessFilter.responseTimeThreshold:"
			+ DEFAULT_RESPONSETIMETHRESHOLD + "}")
	public long responseTimeThreshold = DEFAULT_RESPONSETIMETHRESHOLD;

	@Value("${accessFilter.print:" + DEFAULT_PRINT + "}")
	private boolean print = DEFAULT_PRINT;

	@Value("${accessFilter.excludePatterns:" + DEFAULT_EXCLUDEPATTERNS + "}")
	private String excludePatterns = DEFAULT_EXCLUDEPATTERNS;

	private List<String> excludePatternsList = Collections.EMPTY_LIST;

	private Collection<AccessHandler> handlers;

	@Inject
	private ApplicationContext ctx;

	public void setExcludePatterns(String excludePatterns) {
		this.excludePatterns = excludePatterns;
	}

	public void setPrint(boolean print) {
		this.print = print;
	}

	public long getResponseTimeThreshold() {
		return responseTimeThreshold;
	}

	public void setResponseTimeThreshold(long responseTimeThreshold) {
		this.responseTimeThreshold = responseTimeThreshold;
	}

	@PostConstruct
	public void _init() {
		excludePatternsList = Arrays.asList(excludePatterns.split(","));
		handlers = ctx.getBeansOfType(AccessHandler.class).values();
	}

	public void init(FilterConfig filterConfig) {
		_init();
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		String uri = request.getRequestURI();
		uri = uri.substring(request.getContextPath().length());
		for (String pattern : excludePatternsList) {
			if (org.ironrhino.core.util.StringUtils.matchesWildcard(uri,
					pattern)) {
				chain.doFilter(req, resp);
				return;
			}
		}

		for (AccessHandler interceptor : handlers) {
			String pattern = interceptor.getPattern();
			if (StringUtils.isBlank(pattern)
					|| org.ironrhino.core.util.StringUtils.matchesWildcard(uri,
							pattern)) {
				if (interceptor.handle(request, response))
					return;
			}
		}

		request.setAttribute("userAgent", new UserAgent(request
				.getHeader("User-Agent")));
		MDC.put("remoteAddr", RequestUtils.getRemoteAddr(request));
		MDC.put("method", request.getMethod());
		StringBuilder url = new StringBuilder(request.getRequestURI());
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
		if (print)
			accesLog.info("");

		long start = System.currentTimeMillis();
		chain.doFilter(req, resp);
		long responseTime = System.currentTimeMillis() - start;
		if (responseTime > responseTimeThreshold) {
			StringBuilder sb = new StringBuilder();
			sb.append(RequestUtils.serializeData(request)).append(
					" response time:").append(responseTime).append("ms");
			accesWarnLog.warn(sb.toString());
		}
		MDC.clear();
	}

	public void destroy() {

	}

}
