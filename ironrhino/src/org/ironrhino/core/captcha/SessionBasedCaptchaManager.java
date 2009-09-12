package org.ironrhino.core.captcha;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.metadata.Captcha;
import org.springframework.security.userdetails.UserDetails;

public class SessionBasedCaptchaManager extends AbstractCaptchaManager {

	public static final String SESSION_KEY_CAPTCHA_REQUIRED = "captchaRequired";

	public static final String SESSION_KEY_CAPTCHA_THRESHOLD = "captchaThreshold";

	private boolean firstReachCaptchaThreshold = false;

	@Override
	public void addCaptachaThreshold(HttpServletRequest request) {
		boolean added = request.getAttribute("addCaptachaThreshold") != null;
		if (added)
			return;
		Integer threshold = (Integer) request.getSession(true).getAttribute(
				SESSION_KEY_CAPTCHA_THRESHOLD);
		if (threshold != null)
			threshold += 1;
		else
			threshold = 1;
		request.getSession(true).setAttribute(SESSION_KEY_CAPTCHA_THRESHOLD,
				threshold);
		request.setAttribute("addCaptachaThreshold", true);

	}

	@Override
	public int getCaptachaThreshold(HttpServletRequest request) {
		Integer threshold = (Integer) request.getSession(true).getAttribute(
				SESSION_KEY_CAPTCHA_THRESHOLD);
		return threshold == null ? 0 : threshold;
	}

	@Override
	public void resetCaptachaThreshold(HttpServletRequest request) {
		request.getSession(true).removeAttribute(SESSION_KEY_CAPTCHA_THRESHOLD);

	}

	@Override
	public boolean[] isCaptchaRequired(HttpServletRequest request,
			Captcha captcha) {
		if (captcha != null) {
			if (captcha.always()) {
				return new boolean[] { true, false };
			}
			Boolean b = (Boolean) request.getSession(true).getAttribute(
					SESSION_KEY_CAPTCHA_REQUIRED);
			if (b != null) {
				return new boolean[] { b.booleanValue(), false };
			}
			if (captcha.bypassLoggedInUser()) {
				UserDetails ud = AuthzUtils.getUserDetails(UserDetails.class);
				if (ud != null) {
					return new boolean[] { false, false };
				}
			}
			Integer threshold = getCaptachaThreshold(request);
			if (threshold >= captcha.threshold()) {
				firstReachCaptchaThreshold = (threshold > 0 && threshold == captcha
						.threshold());
				return new boolean[] { true, firstReachCaptchaThreshold };
			}
		}
		return new boolean[] { false, false };
	}

}
