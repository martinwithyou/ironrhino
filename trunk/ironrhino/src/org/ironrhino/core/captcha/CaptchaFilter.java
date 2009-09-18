package org.ironrhino.core.captcha;

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

import org.springframework.beans.factory.annotation.Autowired;

public class CaptchaFilter implements Filter {

	public static final String DEFAULT_IMAGE_CAPTCHA_URL = "/captcha.jpg";

	@Autowired
	private transient CaptchaManager captchaManager;

	private String imageCaptchaUrl = DEFAULT_IMAGE_CAPTCHA_URL;

	public void setImageCaptchaUrl(String imageCaptchaUrl) {
		this.imageCaptchaUrl = imageCaptchaUrl;
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		if (path.equals(imageCaptchaUrl)) {
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			ImageIO.write(
					new ImageCaptcha(captchaManager.getChallenge(request))
							.getImage(), "JPEG", response.getOutputStream());
			response.getOutputStream().close();
			return;
		}
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {

	}

}
