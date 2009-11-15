package org.ironrhino.core.security.captcha.impl;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.metadata.Captcha;
import org.ironrhino.core.security.captcha.CaptchaManager;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("captchaManager")
public class DefaultCaptchaManager implements CaptchaManager {

	private static final String REQUEST_ATTRIBUTE_KEY_CAPTACHA_CHALLENGE = "CAPTCHA_CHALLENGE";
	private static final String REQUEST_ATTRIBUTE_KEY_CAPTACHA_THRESHOLD_ADDED = "CAPTACHA_THRESHOLD_ADDED";
	private static final String REQUEST_ATTRIBUTE_KEY_CAPTACHA_REQUIRED = "CAPTACHA_REQUIRED";
	private static final String REQUEST_ATTRIBUTE_KEY_CAPTACHA_VALIDATED = "CAPTACHA_VALIDATED";

	private static final String CACHE_PREFIX_ANSWER = "answer_";

	public static final String CACHE_PREFIX_THRESHOLD = "captchaThreshold_";

	public static final int CACHE_ANSWER_TIME_TO_LIVE = 60;

	public static final int CACHE_THRESHOLD_TIME_TO_LIVE = 3600;

	private static Random random = new Random();

	@Autowired
	protected CacheManager cacheManager;

	@Override
	public String getChallenge(HttpServletRequest request, String token) {
		String challenge = (String) request
				.getAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_CHALLENGE);
		if (challenge == null) {
			challenge = String.valueOf(random.nextInt(8999) + 1000);// width=60
			String answer = challenge;
			// String challenge;
			// String answer;
			// int left = random.nextInt(89) + 10;
			// int right = random.nextInt(89) + 10;
			// boolean add = (left % 2 == 0);
			// if (add) {
			// challenge = left + "+" + right + "=?";
			// answer = String.valueOf(left + right);
			// } else {
			// if (left <= right) {
			// int temp = right;
			// right = left;
			// left = temp;
			// }
			// challenge = left + "-" + right + "=?";
			// answer = String.valueOf(left - right);
			// }
			cacheManager.put(CACHE_PREFIX_ANSWER + token, answer, -1,
					CACHE_ANSWER_TIME_TO_LIVE, KEY_CAPTCHA);
			request.setAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_CHALLENGE,
					challenge);
		}
		return challenge;
	}

	@Override
	public void addCaptachaThreshold(HttpServletRequest request) {
		boolean added = request
				.getAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_THRESHOLD_ADDED) != null;
		if (!added) {
			String key = getThresholdKey(request);
			Integer threshold = (Integer) cacheManager.get(key, KEY_CAPTCHA);
			if (threshold != null)
				threshold += 1;
			else
				threshold = 1;
			cacheManager.put(key, threshold, -1, CACHE_THRESHOLD_TIME_TO_LIVE,
					KEY_CAPTCHA);
			request.setAttribute(
					REQUEST_ATTRIBUTE_KEY_CAPTACHA_THRESHOLD_ADDED, true);
		}

	}

	@Override
	public boolean[] isCaptchaRequired(HttpServletRequest request,
			Captcha captcha) {
		boolean[] required = (boolean[]) request
				.getAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_REQUIRED);
		if (required == null) {
			if (captcha != null) {
				if (captcha.always()) {
					required = new boolean[] { true, false };
				} else if (captcha.bypassLoggedInUser()) {
					required = new boolean[] {
							AuthzUtils.getUserDetails(UserDetails.class) == null,
							false };
				} else {
					Integer threshold = (Integer) cacheManager.get(
							getThresholdKey(request), KEY_CAPTCHA);
					if (threshold != null && threshold >= captcha.threshold()) {
						required = new boolean[] {
								true,
								(threshold > 0 && threshold == captcha
										.threshold()) };
					} else {
						required = new boolean[] { false, false };
					}
				}
			} else {
				required = new boolean[] { false, false };
			}
			request.setAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_REQUIRED,
					required);
		}
		return required;
	}

	@Override
	public boolean validate(HttpServletRequest request, String token) {
		Boolean validated = (Boolean) request
				.getAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_VALIDATED);
		if (validated == null) {
			String answer = (String) cacheManager.get(CACHE_PREFIX_ANSWER
					+ token, KEY_CAPTCHA);
			boolean b = answer != null
					&& answer.equals(request.getParameter(KEY_CAPTCHA));
			if (b)
				cacheManager.delete(getThresholdKey(request), KEY_CAPTCHA);
			validated = b;
			request.setAttribute(REQUEST_ATTRIBUTE_KEY_CAPTACHA_VALIDATED, b);
		}
		return validated;
	}

	protected String getThresholdKey(HttpServletRequest request) {
		return CACHE_PREFIX_THRESHOLD + RequestUtils.getRemoteAddr(request);
	}
}
