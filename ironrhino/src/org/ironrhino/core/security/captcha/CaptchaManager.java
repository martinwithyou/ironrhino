package org.ironrhino.core.security.captcha;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.core.metadata.Captcha;

public interface CaptchaManager {

	public static final String KEY_CAPTCHA = "captcha";

	public String getChallenge(HttpServletRequest request, String token);

	public boolean validate(HttpServletRequest request, String token);

	/**
	 * array[0] = isCaptchaRequired,array[1] = isFirstReachCaptchaThreshold
	 * 
	 * @param request
	 * @param captcha
	 * @return
	 */
	public boolean[] isCaptchaRequired(HttpServletRequest request,
			Captcha captcha);

	public void addCaptachaThreshold(HttpServletRequest request);

}
