package org.ironrhino.core.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

public class DelegatingFilter extends DelegatingFilterProxy {

	private static Filter dummy = new Filter() {

		@Override
		public void destroy() {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			chain.doFilter(request, response);
		}

		@Override
		public void init(FilterConfig config) throws ServletException {
		}

	};

	protected Filter initDelegate(WebApplicationContext wac)
			throws ServletException {
		try {
			Filter delegate = wac.getBean(getTargetBeanName(), Filter.class);
			if (isTargetFilterLifecycle()) {
				delegate.init(getFilterConfig());
			}
			return delegate;
		} catch (NoSuchBeanDefinitionException e) {
			logger.warn("Use a dummy filter instead: " + e.getMessage());
			return dummy;
		}
	}

}
