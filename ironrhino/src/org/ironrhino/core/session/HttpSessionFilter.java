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

import org.ironrhino.core.performance.BufferableResponseWrapper;

public class HttpSessionFilter implements Filter {

	ServletContext servletContext;

	SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void init(FilterConfig filterConfig) {
		servletContext = filterConfig.getServletContext();
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpContext httpContext = new HttpContext((HttpServletRequest) request,
				(HttpServletResponse) response, servletContext);
		HttpRequest req = new HttpRequest((HttpServletRequest) request,
				httpContext, sessionManager);
		BufferableResponseWrapper res = new BufferableResponseWrapper(
				(HttpServletResponse) response);
		chain.doFilter(req, res);
		byte[] bytes = res.getContents();
		if (bytes != null) {
			sessionManager.save();
			response.setContentLength(bytes.length);
			ServletOutputStream sos = response.getOutputStream();
			sos.write(bytes);
			sos.flush();
			sos.close();
		}
	}

	public void destroy() {

	}
}
