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
import org.springframework.beans.factory.annotation.Autowired;

public class HttpSessionFilter implements Filter {

	ServletContext servletContext;

	@Autowired
	SessionManager sessionManager;

	public void init(FilterConfig filterConfig) {
		servletContext = filterConfig.getServletContext();
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// non struts action
		HttpServletRequest req = (HttpServletRequest) request;
		String uri = req.getRequestURI();
		if (uri.indexOf('.') > 0) {
			chain.doFilter(request, response);
			return;
		}

		HttpContext httpContext = new HttpContext((HttpServletRequest) request,
				(HttpServletResponse) response, servletContext);
		HttpRequest httpRequest = new HttpRequest(req, httpContext,
				sessionManager);
		BufferableResponseWrapper res = new BufferableResponseWrapper(
				(HttpServletResponse) response);
		chain.doFilter(httpRequest, res);
		byte[] bytes = res.getContents();
		if (bytes == null)
			bytes = new byte[0];
		sessionManager.save();
		response.setContentLength(bytes.length);
		ServletOutputStream sos = response.getOutputStream();
		sos.write(bytes);
		sos.flush();
		sos.close();
	}

	public void destroy() {

	}
}
