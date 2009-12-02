package org.ironrhino.core.session;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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

@Singleton
@Named("httpSessionFilter")
public class HttpSessionFilter implements Filter {

	public static final String KEY_EXCLUDE_PATTERNS = "excludePatterns";

	public static final String DEFAULT_EXCLUDE_PATTERNS = "/remoting/*";

	private ServletContext servletContext;

	@Inject
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
			String path = req.getRequestURI();
			path = path.substring(req.getContextPath().length());
			if (org.ironrhino.core.util.StringUtils.matchesWildcard(path,
					pattern)) {
				chain.doFilter(request, response);
				return;
			}
		}
		WrappedHttpSession session = new WrappedHttpSession(
				(HttpServletRequest) request, (HttpServletResponse) response,
				servletContext, httpSessionManager);
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
