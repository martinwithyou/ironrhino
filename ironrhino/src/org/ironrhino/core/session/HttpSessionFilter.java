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
import javax.servlet.http.HttpSession;

import org.ironrhino.core.performance.BufferableResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class HttpSessionFilter implements Filter {

	ServletContext servletContext;

	@Autowired
	HttpSessionManager httpSessionManager;

	public void init(FilterConfig filterConfig) {
		servletContext = filterConfig.getServletContext();
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// non struts action
		HttpServletRequest req = (HttpServletRequest) request;
		HttpContext httpContext = new HttpContext((HttpServletRequest) request,
				(HttpServletResponse) response, servletContext);
		HttpWrappedRequest httpRequest = new HttpWrappedRequest(req,
				httpContext, httpSessionManager);
		BufferableResponseWrapper res = new BufferableResponseWrapper(
				(HttpServletResponse) response);
		// copy from
		// org.springframework.web.context.request.ServletRequestListener
		ServletRequestAttributes attributes = new ServletRequestAttributes(
				httpRequest);
		LocaleContextHolder.setLocale(request.getLocale());
		RequestContextHolder.setRequestAttributes(attributes);

		chain.doFilter(httpRequest, res);

		// copy from
		// org.springframework.web.context.request.ServletRequestListener
		ServletRequestAttributes threadAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if (threadAttributes != null) {
			// We're assumably within the original request thread...
			if (attributes == null) {
				attributes = threadAttributes;
			}
			RequestContextHolder.resetRequestAttributes();
			LocaleContextHolder.resetLocaleContext();
		}
		if (attributes != null) {
			attributes.requestCompleted();
		}
		try {
			byte[] bytes = res.getContents();
			if (bytes == null)
				bytes = new byte[0];
			HttpSession session = httpRequest.getSession();
			if (session instanceof HttpWrappedSession) {
				((HttpWrappedSession) session).save();
			}
			response.setContentLength(bytes.length);
			ServletOutputStream sos = response.getOutputStream();
			if (bytes.length > 0)
				sos.write(bytes);
			sos.flush();
			sos.close();
		} catch (IllegalStateException e) {
		}
	}

	public void destroy() {

	}
}
