package org.ironrhino.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class StringUtils {

	public static boolean matchesWildcard(String text, String pattern) {
		text += '\0';
		pattern += '\0';

		int N = pattern.length();

		boolean[] states = new boolean[N + 1];
		boolean[] old = new boolean[N + 1];
		old[0] = true;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			states = new boolean[N + 1];
			for (int j = 0; j < N; j++) {
				char p = pattern.charAt(j);

				if (old[j] && (p == '*'))
					old[j + 1] = true;

				if (old[j] && (p == c))
					states[j + 1] = true;
				if (old[j] && (p == '?'))
					states[j + 1] = true;
				if (old[j] && (p == '*'))
					states[j] = true;
				if (old[j] && (p == '*'))
					states[j + 1] = true;
			}
			old = states;
		}
		return states[N];
	}

	public static boolean matchesAutocomplete(String text, String pattern) {

		text = text.toLowerCase();
		pattern = pattern.toLowerCase();
		if (text.startsWith(pattern))
			return true;
		char[] chars = pattern.toCharArray();
		if (chars.length <= text.length()) {
			boolean b1 = true;
			for (int i = 0; i < chars.length; i++) {
				boolean b2 = false;
				for (String s : pinyin(text.charAt(i)))
					if (s.indexOf(chars[i]) == 0) {
						b2 = true;
						break;
					}
				if (!b2) {
					b1 = false;
					break;
				}
			}
			if (b1)
				return true;
		}
		// matches string array split by space
		String[] array = pattern.split("\\s");
		if (array.length > 1) {
			boolean b1 = true;
			for (int i = 0; i < array.length; i++) {
				boolean b2 = false;
				for (String s : pinyin(text.charAt(i)))
					if (s.indexOf(array[i]) == 0) {
						b2 = true;
						break;
					}
				if (!b2) {
					b1 = false;
					break;
				}
			}
			if (b1)
				return true;
		}
		// matches string increasingly,may be not restricted
		int matchedChars = 0;
		int matchedPosition = 0;
		while (pattern.length() - 1 > matchedPosition) {
			boolean b = false;
			for (String s : pinyin(text.charAt(matchedChars)))
				if (pattern.substring(matchedPosition).indexOf(s) == 0) {
					b = true;
					matchedPosition += s.length();
					break;
				}
			if (!b)
				return false;
			matchedChars++;
		}
		return true;
	}

	private static String[] pinyin(char c) {
		HanyuPinyinOutputFormat f = new HanyuPinyinOutputFormat();
		f.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		f.setVCharType(HanyuPinyinVCharType.WITH_V);
		f.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		String[] array = null;
		try {
			array = PinyinHelper.toHanyuPinyinStringArray(c, f);
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		if (array == null)// not chinese
			array = new String[] { String.valueOf(c) };
		return array;
	}

}
