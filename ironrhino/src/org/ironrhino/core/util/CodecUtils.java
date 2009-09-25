package org.ironrhino.core.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class CodecUtils {

	public static String encode(String input) {
		byte[] bytes = new byte[0];
		try {
			bytes = Base64.encodeBase64(input.getBytes("UTF-8"));
			swap(bytes);
		} catch (UnsupportedEncodingException e) {
		}
		return new String(bytes);
	}

	public static String decode(String input) {
		byte[] bytes = input.getBytes();
		swap(bytes);
		try {
			return new String(Base64.decodeBase64(bytes), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	private static void swap(byte[] bytes) {
		int half = bytes.length / 2;
		for (int i = 0; i < half; i++) {
			byte temp = bytes[i];
			bytes[i] = bytes[half + i];
			bytes[half + i] = temp;
		}
	}

	public static String digest(String input) {
		return DigestUtils.shaHex(md5(input, 3));
	}

	public static String md5(String input, int times) {
		for (int i = 0; i < times; i++)
			input = DigestUtils.md5Hex(input);
		return input;
	}

	private static String string = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String randomString(int digits) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digits; i++)
			sb.append(string.charAt(random.nextInt(26)));
		return sb.toString();
	}
}
