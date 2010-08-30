package org.ironrhino.core.security.util;

import java.io.File;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.util.AppInfo;

public class Blowfish {
	private static Log log = LogFactory.getLog(Blowfish.class);

	public static final String DEFAULT_KEY_LOCATION = "/resources/key/blowfish";
	public static final String KEY_DIRECTORY = "/key/";

	private static String CIPHER_NAME = "Blowfish/CFB8/NoPadding";
	private static String KEY_SPEC_NAME = "Blowfish";
	// thread safe
	private static final ThreadLocal<Blowfish> pool = new ThreadLocal<Blowfish>() {
		@Override
		protected Blowfish initialValue() {
			return new Blowfish();
		}
	};
	
	private static String defaultKey = "youcannotguessme";
	private Cipher enCipher;
	private Cipher deCipher;

	static {
		try {
			File file = new File(AppInfo.getAppHome() + KEY_DIRECTORY
					+ "blowfish");
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

	public Blowfish() {
		this(defaultKey);
	}

	public Blowfish(String key) {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(),
					KEY_SPEC_NAME);
			IvParameterSpec ivParameterSpec = new IvParameterSpec((DigestUtils
					.md5Hex(key).substring(0, 8)).getBytes());
			enCipher = Cipher.getInstance(CIPHER_NAME);
			deCipher = Cipher.getInstance(CIPHER_NAME);
			enCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			deCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		} catch (Exception e) {
			log.error("[BlowfishEncrypter]", e);
		}
	}

	public static String encrypt(String str) {
		if (str == null)
			return null;
		try {
			return new String(Base64.encodeBase64(pool.get().encrypt(
					str.getBytes("UTF-8"))), "UTF-8");
		} catch (Exception ex) {
			log.error("encrypt exception!", ex);
			return "";
		}
	}

	public static String decrypt(String str) {
		if (str == null)
			return null;
		try {
			return new String(pool.get().decrypt(
					Base64.decodeBase64(str.getBytes("UTF-8"))), "UTF-8");
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return "";
		}
	}

	public static String encryptBytesToString(byte[] bytes) {
		try {
			return new String(Base64.encodeBase64(pool.get().encrypt(bytes)),
					"UTF-8");
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return "";
		}
	}

	public static String encryptBytesToString(byte[] bytes, int offset,
			int length) {
		try {
			return new String(Base64.encodeBase64(pool.get().encrypt(bytes,
					offset, length)), "UTF-8");
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return "";
		}
	}

	public static byte[] decryptStringToBytes(String str) {
		try {
			return pool.get().decrypt(
					Base64.decodeBase64(str.getBytes("UTF-8")));
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return new byte[0];
		}
	}

	public byte[] encrypt(byte[] bytes) throws IllegalBlockSizeException,
			BadPaddingException {
		return enCipher.doFinal(bytes);
	}

	public byte[] encrypt(byte[] bytes, int offset, int length)
			throws IllegalBlockSizeException, BadPaddingException {
		return enCipher.doFinal(bytes, offset, length);
	}

	public byte[] decrypt(byte[] bytes) throws IllegalBlockSizeException,
			BadPaddingException {
		return deCipher.doFinal(bytes);
	}

	public byte[] decrypt(byte[] bytes, int offset, int length)
			throws IllegalBlockSizeException, BadPaddingException {
		return deCipher.doFinal(bytes, offset, length);
	}

}
