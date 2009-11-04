package org.ironrhino.core.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class CodecUtils {

	private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("md5 algorythm found");
			}
		}
	};

	private static ThreadLocal<MessageDigest> SHA = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("SHA");
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("sha algorythm found");
			}
		}
	};

	public static byte[] md5(byte[] input) {
		MessageDigest md5 = MD5.get();
		md5.reset();
		md5.update(input);
		return md5.digest();
	}

	public static byte[] md5(String input) {
		return md5(input.getBytes());
	}

	public static String md5Hex(byte[] input) {
		return Hex.encodeHexString(md5(input));
	}

	public static String md5Hex(String input) {
		return Hex.encodeHexString(md5(input.getBytes()));
	}

	public static byte[] sha(byte[] input) {
		MessageDigest sha = SHA.get();
		sha.reset();
		sha.update(input);
		return sha.digest();
	}

	public static byte[] sha(String input) {
		return sha(input.getBytes());
	}

	public static String shaHex(byte[] input) {
		return Hex.encodeHexString(sha(input));
	}

	public static String shaHex(String input) {
		return Hex.encodeHexString(sha(input.getBytes()));
	}

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
		return md5Hex(shaHex(input, 3));
	}

	public static String md5Hex(String input, int times) {
		for (int i = 0; i < times; i++)
			input = md5Hex(input);
		return input;
	}

	public static String shaHex(String input, int times) {
		for (int i = 0; i < times; i++)
			input = shaHex(input);
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

	public static String nextId() {
		String id = UUID.randomUUID().toString().replace("-", "");
		id = NumberUtils.xToY(16, 62, id);
		return id;
	}

	public static String nextId(String salt) {
		String id = md5Hex(salt + UUID.randomUUID().toString());
		id = NumberUtils.xToY(16, 62, id);
		return id;
	}

}
