package org.ironrhino.core.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.performance.BufferableResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("httpSessionFilter")
public class HttpSessionFilter implements Filter {

	public static final String KEY_EXCLUDE_PATTERNS = "excludePatterns";

	public static final String DEFAULT_EXCLUDE_PATTERNS = "/assets/*,/remoting/*";

	private ServletContext servletContext;

	@Autowired
	private HttpSessionManager httpSessionManager;

	private String[] excludePatterns;

	public void init(FilterConfig filterConfig) {
		servletContext = filterConfig.getServletContext();
		String str = filterConfig.getInitParameter(KEY_EXCLUDE_PATTERNS);
		if (StringUtils.isBlank(str))
			str = DEFAULT_EXCLUDE_PATTERNS;
		excludePatterns = str.split(",");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		for (String pattern : excludePatterns) {
			if (org.ironrhino.core.util.StringUtils.matchesWildcard(req
					.getServletPath(), pattern)) {
				chain.doFilter(request, response);
				return;
			}
		}
		HttpContext httpContext = new HttpContext((HttpServletRequest) request,
				(HttpServletResponse) response, servletContext);
		WrappedHttpSession session = new WrappedHttpSession(httpContext,
				httpSessionManager);
		HttpServletRequest wrappedHttpRequest = new WrappedHttpServletRequest(
				req, session);
		HttpServletResponse wrappedHttpResponse = new WrappedHttpServletResponse(
				(HttpServletResponse) response, session);
		BufferableResponseWrapper buffResponse = new BufferableResponseWrapper(
				wrappedHttpResponse);
		chain.doFilter(wrappedHttpRequest, buffResponse);
		try {
			byte[] bytes = buffResponse.getContents();
			if (bytes == null)
				bytes = new byte[0];
			session.save();
			response.setContentLength(bytes.length);
			ServletOutputStream sos = response.getOutputStream();
			if (bytes.length > 0)
				sos.write(bytes);
			sos.flush();
			sos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() {

	}
}
