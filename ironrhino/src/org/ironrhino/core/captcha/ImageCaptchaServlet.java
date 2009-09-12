package org.ironrhino.core.captcha;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationContextUtils;

public class ImageCaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 1195646317465747357L;

	private transient CaptchaManager captchaManager;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		ImageIO.write(captchaManager.getChallengeImage(request), "JPEG",
				response.getOutputStream());
		response.getOutputStream().close();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		captchaManager = (CaptchaManager) WebApplicationContextUtils
				.getWebApplicationContext(getServletContext()).getBean(
						"captchaManager");
	}

}
