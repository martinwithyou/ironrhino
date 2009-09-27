package org.ironrhino.core.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * notice HttpWrappedSession.save() called before sitemesh apply decorator
 * 
 * @see org.ironrhino.core.struts.HookedFreeMarkerPageFilter
 * @author minggao
 * 
 */
@Component("httpSessionFilter")
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

		// copy from
		// org.springframework.web.context.request.ServletRequestListener
		ServletRequestAttributes attributes = new ServletRequestAttributes(
				httpRequest);
		LocaleContextHolder.setLocale(request.getLocale());
		RequestContextHolder.setRequestAttributes(attributes);

		chain.doFilter(httpRequest, response);

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
	}

	public void destroy() {

	}
}
