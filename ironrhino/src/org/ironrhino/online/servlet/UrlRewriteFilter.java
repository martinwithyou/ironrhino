package org.ironrhino.online.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
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
import org.ironrhino.core.util.Thumbnail;
import org.springframework.stereotype.Component;

@Component("urlRewriteFilter")
public class UrlRewriteFilter implements Filter {

	protected Log log = LogFactory.getLog(UrlRewriteFilter.class);

	private String picPrefix = "/pic/";

	private String productPrefix = "/product/";

	private ServletContext servletContext;

	public void setPicPrefix(String picPrefix) {
		this.picPrefix = picPrefix;
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

		if (path.startsWith(picPrefix)) {
			String file = path.substring(path.lastIndexOf('/') + 1);
			if (file.indexOf('.') == file.lastIndexOf('.')) {
				// original pic,no need thumbnail
				chain.doFilter(req, resp);
			} else {
				String size = file.substring(file.indexOf('.') + 1, file
						.lastIndexOf('.'));
				file = file.substring(0, file.indexOf('.'))
						+ file.substring(file.lastIndexOf('.'));
				if (size.equalsIgnoreCase("s"))
					size = "100x100";
				else if (size.equalsIgnoreCase("m"))
					size = "200x200";
				size = size.toLowerCase();
				try {
					int width = Integer.valueOf(size.substring(0, size
							.indexOf('x')));
					int height = Integer.valueOf(size.substring(size
							.indexOf('x') + 1));
					File f = new File(servletContext.getRealPath(picPrefix
							+ file));
					if (!f.exists())
						f = new File(servletContext
								.getRealPath("/images/product.jpg"));
					BufferedImage image = ImageIO.read(f);
					image = Thumbnail.resizeFix(image, width, height);
					response.setHeader("Cache-Control", "max-age=86400");
					ImageIO.write(image, file
							.substring(file.lastIndexOf('.') + 1), response
							.getOutputStream());
				} catch (Exception e) {
					log.error(path + ":" + e.getMessage(), e);
					chain.doFilter(req, resp);
				}
			}
			return;
		}

		if (!path.endsWith(".html")) {
			chain.doFilter(req, resp);
			return;
		}
		productCode = path.substring(productPrefix.length(), path
				.lastIndexOf('.'));
		request.getRequestDispatcher(productPrefix + "view/" + productCode)
				.forward(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		servletContext = config.getServletContext();
	}

}
