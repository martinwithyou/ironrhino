package org.ironrhino.core.captcha.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.captcha.CaptchaManager;
import org.ironrhino.core.metadata.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.userdetails.UserDetails;

public class DefaultCaptchaManager implements CaptchaManager {

	private static final String CACHE_PREFIX_ANSWER = "answer_";

	public static final String CACHE_PREFIX_THRESHOLD = "captchaThreshold_";

	public static final int CACHE_ANSWER_TIME_TO_LIVE = 60;

	public static final int CACHE_THRESHOLD_TIME_TO_LIVE = 3600;

	public static final int width = 200;// 80

	public static final int height = 50;

	private static Random random = new Random();

	private static List<String> fonts = new ArrayList<String>();

	@Autowired
	protected CacheManager cacheManager;

	static {
		GraphicsEnvironment.getLocalGraphicsEnvironment().preferLocaleFonts();
		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames(Locale.CHINA);
		for (String s : names) {
			char c = s.charAt(0);
			if (Character.isLowerCase(c) || Character.isUpperCase(c)) {
			} else {
				fonts.add(s);
			}
		}
	}

	private static Stroke getStroke() {
		BasicStroke bs = new BasicStroke((float) (Math.random() * 3));
		return bs;
	}

	private static Point getPoint(int index) {
		return new Point(5 + (index * (random.nextInt(10) + 40)), 40);
	}

	private static Paint getPaint(Point p, int size) {
		GradientPaint gp = new GradientPaint(p.x, p.y, new Color(random
				.nextInt(256), 0, random.nextInt(256)), p.x, p.y - size,
				new Color(random.nextInt(256), random.nextInt(256), random
						.nextInt(256)));
		return gp;
	}

	private static int getFace() {
		if (Math.random() * 10 > 5) {
			return Font.BOLD;
		} else {
			return Font.ITALIC;
		}
	}

	private static int getSize() {
		int[] sizes = new int[20];
		for (int i = 0; i < 20; i++) {
			sizes[i] = 30 + i;
		}
		return sizes[(int) (Math.random() * sizes.length)];
	}

	protected String getChallenge(HttpServletRequest request) {
		String challenge = String.valueOf(random.nextInt(8999) + 1000);// width=60
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
		cacheManager.put(getAnswerKey(request), answer, -1,
				CACHE_ANSWER_TIME_TO_LIVE, KEY_CAPTCHA);
		return challenge;
	}

	@Override
	public BufferedImage getChallengeImage(HttpServletRequest request) {
		BufferedImage challengeImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = challengeImage.createGraphics();
		char[] use = getChallenge(request).toCharArray();
		g.setColor(new Color(240, 240, 240));
		g.fillRect(0, 0, 200, 50);
		for (int i = 0; i < use.length; i++) {
			Point p = getPoint(i);
			int size = getSize();
			// g.setColor(new
			// Color((int)(Math.random()*256),0,(int)(Math.random()*256)));
			g.setPaint(getPaint(p, size));
			g.setFont(new Font(fonts.get((int) (Math.random() * fonts.size())),
					getFace(), size));
			g.drawString("" + use[i], p.x, p.y);
		}
		g.setStroke(new BasicStroke(1.0f));
		g.setPaint(null);
		for (int i = 0; i < use.length; i++) {
			g.setColor(new Color(random.nextInt(0x00FFFFFFF)));
			g.drawLine(random.nextInt(200), random.nextInt(50), random
					.nextInt(200), random.nextInt(50));
		}
		Random random = new Random();
		for (int i = 0; i < 88; i++) {
			int x = random.nextInt(200);
			int y = random.nextInt(50);
			g.setColor(new Color(random.nextInt(0x00FFFFFFF)));
			g.setStroke(getStroke());
			g.drawLine(x, y, x, y);
		}

		g.dispose();

		return challengeImage;
	}

	@Override
	public void addCaptachaThreshold(HttpServletRequest request) {
		boolean added = request.getAttribute("addCaptachaThreshold") != null;
		if (added)
			return;
		String key = getThresholdKey(request);
		Integer threshold = (Integer) cacheManager.get(key, KEY_CAPTCHA);
		if (threshold != null)
			threshold += 1;
		else
			threshold = 1;
		cacheManager.put(key, threshold, -1, CACHE_THRESHOLD_TIME_TO_LIVE,
				KEY_CAPTCHA);
		request.setAttribute("addCaptachaThreshold", true);

	}

	@Override
	public boolean[] isCaptchaRequired(HttpServletRequest request,
			Captcha captcha) {
		if (captcha != null) {
			if (captcha.always()) {
				return new boolean[] { true, false };
			}
			if (captcha.bypassLoggedInUser())
				return new boolean[] {
						AuthzUtils.getUserDetails(UserDetails.class) == null,
						false };
			Integer threshold = (Integer) cacheManager.get(
					getThresholdKey(request), KEY_CAPTCHA);
			if (threshold != null && threshold >= captcha.threshold()) {
				return new boolean[] { true,
						(threshold > 0 && threshold == captcha.threshold()) };
			}
		}
		return new boolean[] { false, false };
	}

	@Override
	public boolean validate(HttpServletRequest request) {
		String answer = (String) cacheManager.get(getAnswerKey(request),
				KEY_CAPTCHA);
		boolean b = answer != null
				&& answer.equals(request.getParameter(KEY_CAPTCHA));
		if (b)
			cacheManager.delete(getThresholdKey(request), KEY_CAPTCHA);
		return b;
	}

	protected String getAnswerKey(HttpServletRequest request) {
		return CACHE_PREFIX_ANSWER + request.getSession().getId();
	}

	protected String getThresholdKey(HttpServletRequest request) {
		return CACHE_PREFIX_THRESHOLD + request.getRemoteAddr();
	}
}
