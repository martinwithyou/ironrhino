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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.ironrhino.core.spring.security.DefaultAuthenticationSuccessHandler;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.core.util.UserAgent;
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
	public long responseTimeThreshold = DEFAULT_RESPONSETIMETHRESHOLD;

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

	public long getResponseTimeThreshold() {
		return responseTimeThreshold;
	}

	public void setResponseTimeThreshold(long responseTimeThreshold) {
		this.responseTimeThreshold = responseTimeThreshold;
	}

	@PostConstruct
	public void _init() {
		excludePatternsList = Arrays.asList(excludePatterns.split(","));
	}

	public void init(FilterConfig filterConfig) {
		_init();
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// handle cross origin request
		String origin = request.getHeader("Origin");
		if (StringUtils.isNotBlank(origin)) {
			if ("Upgrade".equalsIgnoreCase(request.getHeader("Connection"))
					&& "WebSocket".equalsIgnoreCase(request
							.getHeader("Upgrade")))
				return;
			String url = request.getRequestURL().toString();
			if (RequestUtils.isSameOrigin(url, origin)
					&& !url.startsWith(origin)) {
				response.setHeader("Access-Control-Allow-Origin", origin);
				response.setHeader("Access-Control-Allow-Credentials", "true");
				String requestMethod = request
						.getHeader("Access-Control-Request-Method");
				String requestHeaders = request
						.getHeader("Access-Control-Request-Headers");
				String method = request.getMethod();
				if (method.equalsIgnoreCase("OPTIONS")
						&& (requestMethod != null || requestHeaders != null)) {
					// preflighted request
					if (StringUtils.isNotBlank(requestMethod))
						response.setHeader("Access-Control-Allow-Methods",
								requestMethod);
					if (StringUtils.isNotBlank(requestHeaders))
						response.setHeader("Access-Control-Allow-Headers",
								requestHeaders);
					response.setHeader("Access-Control-Max-Age", "36000");
					return;
				}
			}
		}

		request.setAttribute("userAgent", new UserAgent(request
				.getHeader("User-Agent")));
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
		if (responseTime > responseTimeThreshold) {
			StringBuilder sb = new StringBuilder();
			sb.append(RequestUtils.serializeData(request)).append(
					" response time:").append(responseTime).append("ms");
			accesWarnLog.warn(sb.toString());
		}
		MDC.getContext().clear();
	}

	public void destroy() {

	}

}
