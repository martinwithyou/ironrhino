package org.ironrhino.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class NumberUtils {

	public static String format(int value, int digit) {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);
		nf.setMinimumIntegerDigits(digit);
		return nf.format(value);
	}

	public static String format(double value, int fractionDigits) {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);
		nf.setMinimumFractionDigits(fractionDigits);
		return nf.format(value);
	}

	public static double round(double value, int decimalDigits) {
		return round(new BigDecimal(value), decimalDigits).doubleValue();
	}

	public static BigDecimal round(BigDecimal bd, int decimalDigits) {
		if (bd == null)
			return null;
		int precision = decimalDigits;
		String s = String.valueOf(bd.intValue());
		if (!s.equals("0"))
			precision += s.length();
		return new BigDecimal(bd.round(
				new MathContext(precision, RoundingMode.HALF_UP)).doubleValue());
	}

	public static String formatPercent(double value, int fractionDigits) {
		value *= 100;
		return format(value, fractionDigits) + "%";
	}

	public static final String NUMBERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

	public static String decimalToX(int scale, BigInteger decimalValue) {
		if (scale > NUMBERS.length())
			throw new IllegalArgumentException("n must less or equal to "
					+ NUMBERS.length());
		BigInteger bscale = BigInteger.valueOf(scale);
		boolean negative = decimalValue.compareTo(BigInteger.valueOf(0)) < 0;
		if (negative)
			decimalValue = decimalValue.abs();
		StringBuilder sb = new StringBuilder();

		String sub = NUMBERS.substring(0, scale);
		while (decimalValue.compareTo(BigInteger.valueOf(0)) != 0) {
			BigInteger b = decimalValue.mod(bscale);
			sb.insert(0, sub.charAt(b.intValue()));
			decimalValue = decimalValue.add(b.negate()).divide(bscale);
		}
		if (negative)
			sb.insert(0, '-');
		return sb.toString();
	}

	public static BigInteger xToDecimal(int scale, String xScaleValue) {
		if (scale > NUMBERS.length())
			throw new IllegalArgumentException("n must less or equal to "
					+ NUMBERS.length());
		BigInteger bscale = BigInteger.valueOf(scale);
		String a = NUMBERS.substring(0, scale);
		BigInteger c = BigInteger.valueOf(1);
		BigInteger t = BigInteger.valueOf(0);
		for (int x = xScaleValue.length() - 1; x > -1; x--) {
			t = t.add(c.multiply(BigInteger.valueOf(a.indexOf(xScaleValue
					.charAt(x)))));
			c = c.multiply(bscale);
		}
		return t;
	}

	public static String xToY(int xScale, int yScale, String value) {
		return decimalToX(yScale, xToDecimal(xScale, value));
	}

}
