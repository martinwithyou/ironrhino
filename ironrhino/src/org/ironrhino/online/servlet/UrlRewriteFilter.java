package org.ironrhino.online.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlRewriteFilter implements Filter {

	protected Log log = LogFactory.getLog(UrlRewriteFilter.class);

	private String productPrefix = "/product/";

	public String getProductPrefix() {
		return productPrefix;
	}

	public void setProductPrefix(String productPrefix) {
		this.productPrefix = productPrefix;
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String productCode = "";
		String path = request.getServletPath();
		if (path == null) {
			path = request.getRequestURI();
			path = path.substring(request.getContextPath().length());
		}
		if (!path.endsWith(".html")){
			chain.doFilter(req, resp);
			return;
		}
		productCode = path.substring(productPrefix.length(), path
				.lastIndexOf('.'));
		request.getRequestDispatcher(productPrefix + "view/" + productCode)
				.forward(request, response);
	}

	public void init(FilterConfig config) throws ServletException {

	}
}
