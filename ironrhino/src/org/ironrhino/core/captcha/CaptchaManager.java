package org.ironrhino.core.captcha;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.core.metadata.Captcha;

public interface CaptchaManager {

	public static final String KEY_CAPTCHA = "captcha";

	public BufferedImage getChallengeImage(HttpServletRequest request);

	public boolean validate(HttpServletRequest request);

	public boolean[] isCaptchaRequired(HttpServletRequest request,
			Captcha captcha);

	public int getCaptachaThreshold(HttpServletRequest request);

	public void addCaptachaThreshold(HttpServletRequest request);

	public void resetCaptachaThreshold(HttpServletRequest request);

}
