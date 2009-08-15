package org.ironrhino.common.util;

import java.math.BigDecimal;
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
	
	public static void main(String...strings){
		System.out.println(formatPercent(0.1234567, 2));
	}

}
