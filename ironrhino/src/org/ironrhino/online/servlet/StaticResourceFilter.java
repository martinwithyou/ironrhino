package org.ironrhino.online.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.pms.model.Product;
import org.springframework.beans.factory.annotation.Autowired;

public class StaticResourceFilter implements Filter {

	public static final String SETTING_KEY_USESTATICPAGE = "staticResourceFilter.useStaticPage";

	protected Log log = LogFactory.getLog(StaticResourceFilter.class);

	private String picPrefix = "/pic/";

	private String productPrefix = "/product/";

	@Autowired
	private ProductFacade productFacade;

	@Autowired
	private SettingControl settingControl;

	private ServletContext servletContext;

	public String getPicPrefix() {
		return picPrefix;
	}

	public void setPicPrefix(String picPrefix) {
		this.picPrefix = picPrefix;
	}

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
		boolean viewPicture = false, viewProduct = false;
		String productCode = "";
		String path = request.getServletPath();
		if (path == null) {
			path = request.getRequestURI();
			path = path.substring(request.getContextPath().length());
		}
		if (path.startsWith(picPrefix) && path.endsWith(".jpg")) {
			viewPicture = true;
			if (path.endsWith(".small.jpg"))
				productCode = path.substring(picPrefix.length(), path.length()
						- ".small.jpg".length());
			else if (path.endsWith(".medium.jpg"))
				productCode = path.substring(picPrefix.length(), path.length()
						- ".medium.jpg".length());
			else
				productCode = path.substring(picPrefix.length(), path.length()
						- ".jpg".length());
			if (productCode.lastIndexOf('_') > 0)
				productCode = productCode.substring(0, productCode
						.lastIndexOf('_'));
		} else if (path.startsWith(productPrefix) && path.endsWith(".html")) {
			viewProduct = true;
			productCode = path.substring(productPrefix.length(), path
					.lastIndexOf('.'));
		}
		// other request
		if (!viewPicture && !viewProduct) {
			chain.doFilter(req, resp);
			return;
		}
		Product product = productFacade.getProductByCode(productCode);
		boolean notFound = (product == null)
				|| (product.getOpen() != null ? !product.getOpen() : true);
		if (notFound) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (settingControl.getBooleanValue(SETTING_KEY_USESTATICPAGE)
				|| viewPicture) {
			if (viewPicture && !product.isPictured())
				response.sendRedirect(request.getContextPath()
						+ "/images/product.jpg");
			else
				chain.doFilter(req, resp);
			return;
		}

		File file = new File(servletContext.getRealPath(productPrefix
				+ productCode + ".html"));
		if (file.exists()) {
			// static page
			response.setContentType("text/html");
			// httpResponse.setHeader("Cache-Control", "no-cache");
			// httpResponse.setHeader("Pragma", "no-cache");
			// httpResponse.setHeader("Expires", "-1");
			InputStream in = null;
			OutputStream out = response.getOutputStream();
			try {
				in = new FileInputStream(file);
				final byte[] buffer = new byte[4096];
				int n;
				while (-1 != (n = in.read(buffer))) {
					out.write(buffer, 0, n);
				}
			} finally {
				if (in != null)
					in.close();
				out.flush();
				out.close();
			}
		} else {
			log.warn("product[" + productCode
					+ "] has not generated static page");
			request.getRequestDispatcher(productPrefix + "view/" + productCode)
					.forward(request, response);
			return;
		}
	}

	public void init(FilterConfig config) throws ServletException {
		servletContext = config.getServletContext();
	}
}
