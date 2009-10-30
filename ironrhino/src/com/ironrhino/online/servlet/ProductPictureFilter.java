package com.ironrhino.online.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
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
import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.util.Thumbnail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("productPictureFilter")
public class ProductPictureFilter implements Filter {

	protected Log log = LogFactory.getLog(ProductPictureFilter.class);

	private String productPrefix = "/product/";

	@Autowired
	private FileStorage fileStorage;

	public void setProductPrefix(String productPrefix) {
		this.productPrefix = productPrefix;
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		if (path == null) {
			path = request.getRequestURI();
			path = path.substring(request.getContextPath().length());
		}
		if (path.startsWith(productPrefix) && path.endsWith(".jpg")) {
			String file = path.substring(path.lastIndexOf('/') + 1);
			if (file.indexOf('.') == file.lastIndexOf('.')) {
				// original pic,no need thumbnail
				try {
					fileStorage.write(path, response.getOutputStream());
				} catch (Exception e) {
					request.getRequestDispatcher("/assets/images/product.jpg")
							.forward(request, response);
				}
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
				int width = Integer.valueOf(size
						.substring(0, size.indexOf('x')));
				int height = Integer.valueOf(size
						.substring(size.indexOf('x') + 1));
				path = productPrefix + file;
				try {
					BufferedImage image = ImageIO.read(fileStorage.open(path));
					image = Thumbnail.resizeFix(image, width, height);
					response.setHeader("Cache-Control", "max-age=86400");
					ImageIO.write(image, file
							.substring(file.lastIndexOf('.') + 1), response
							.getOutputStream());
				} catch (Exception e) {
					request.getRequestDispatcher("/assets/images/product.jpg")
							.forward(request, response);
				}
			}
			return;
		}
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {

	}

	public void destroy() {
	}

}
