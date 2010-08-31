package org.ironrhino.core.security.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.util.AppInfo;

public class RC4 {
	private static Log log = LogFactory.getLog(RC4.class);

	public static final String DEFAULT_KEY_LOCATION = "/resources/key/rc4";
	public static final String KEY_DIRECTORY = "/key/";

	private static String defaultKey = "youcannotguessme";

	static {
		try {
			File file = new File(AppInfo.getAppHome() + KEY_DIRECTORY
					+ "rc4");
			if (file.exists()) {
				defaultKey = FileUtils.readFileToString(file, "UTF-8");
			} else {
				log.warn("[" + file
						+ "] doesn't exists,use classpath resources "
						+ DEFAULT_KEY_LOCATION);
				defaultKey = IOUtils.toString(Blowfish.class
						.getResourceAsStream(DEFAULT_KEY_LOCATION), "UTF-8");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private byte state[] = new byte[256];
	private int x;
	private int y;

	public RC4() {
		this(defaultKey);
	}

	public RC4(String key) {
		this(key.getBytes());
	}

	public RC4(byte[] key) {
		for (int i = 0; i < 256; i++) {
			state[i] = (byte) i;
		}
		x = 0;
		y = 0;
		int index1 = 0;
		int index2 = 0;
		byte tmp;
		if (key == null || key.length == 0) {
			throw new NullPointerException();
		}
		for (int i = 0; i < 256; i++) {
			index2 = ((key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
			tmp = state[i];
			state[i] = state[index2];
			state[index2] = tmp;
			index1 = (index1 + 1) % key.length;
		}
	}

	public byte[] rc4(String data) {
		if (data == null) {
			return null;
		}
		byte[] tmp = data.getBytes();
		this.rc4(tmp);
		return tmp;
	}

	public byte[] rc4(byte[] buf) {
		int xorIndex;
		byte tmp;
		if (buf == null) {
			return null;
		}
		byte[] result = new byte[buf.length];
		for (int i = 0; i < buf.length; i++) {
			x = (x + 1) & 0xff;
			y = ((state[x] & 0xff) + y) & 0xff;
			tmp = state[x];
			state[x] = state[y];
			state[y] = tmp;
			xorIndex = ((state[x] & 0xff) + (state[y] & 0xff)) & 0xff;
			result[i] = (byte) (buf[i] ^ state[xorIndex]);
		}
		return result;
	}

	public static String encrypt(String input)
			throws UnsupportedEncodingException {
		if (input == null)
			return null;
		RC4 rc4 = new RC4(defaultKey);
		return Hex.encodeHexString(rc4.rc4(URLEncoder.encode(input, "UTF-8")
				.getBytes("UTF-8")));
	}

	public static String decrypt(String input)
			throws UnsupportedEncodingException, DecoderException {
		if (input == null)
			return null;
		RC4 rc4 = new RC4(defaultKey);
		return URLDecoder.decode(new String(rc4.rc4(Hex.decodeHex(input
				.toCharArray())), "UTF-8"), "UTF-8");
	}

	public static String encrypt(String input, String key)
			throws UnsupportedEncodingException {
		if (key == null || input == null)
			return null;
		RC4 rc4 = new RC4(key);
		return Hex.encodeHexString(rc4.rc4(URLEncoder.encode(input, "UTF-8")
				.getBytes("UTF-8")));
	}

	public static String decrypt(String input, String key)
			throws UnsupportedEncodingException, DecoderException {
		if (key == null || input == null)
			return null;
		RC4 rc4 = new RC4(key);
		return URLDecoder.decode(new String(rc4.rc4(Hex.decodeHex(input
				.toCharArray())), "UTF-8"), "UTF-8");
	}

}
