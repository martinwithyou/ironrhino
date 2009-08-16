package org.ironrhino.core.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

public class Blowfish {
	private static Log log = LogFactory.getLog(Blowfish.class);

	public static final String KEY_LOCATION = "/resources/key/blowfish";
	private static String CIPHER_KEY = "youcannotguessme";
	private static String CIPHER_NAME = "Blowfish/CFB8/NoPadding";
	private static String KEY_SPEC_NAME = "Blowfish";
	private static SecretKeySpec secretKeySpec = null;
	private static IvParameterSpec ivParameterSpec = null;
	// thread safe
	private static final ThreadLocal<Blowfish> pool = new ThreadLocal<Blowfish>();
	Cipher enCipher;
	Cipher deCipher;

	static {
		try {
			CIPHER_KEY = new String(StreamUtils.getBytes(Blowfish.class
					.getResourceAsStream(KEY_LOCATION)), "UTF-8");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private Blowfish() {
		try {
			secretKeySpec = new SecretKeySpec(CIPHER_KEY.getBytes(),
					KEY_SPEC_NAME);
			ivParameterSpec = new IvParameterSpec((DigestUtils
					.md5Hex(CIPHER_KEY).substring(0, 8)).getBytes());
			enCipher = Cipher.getInstance(CIPHER_NAME);
			deCipher = Cipher.getInstance(CIPHER_NAME);
			enCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			deCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		} catch (Exception e) {
			log.error("[BlowfishEncrypter]", e);
		}
	}

	public static Blowfish get() {
		Blowfish encrypter = pool.get();
		if (encrypter == null) {
			encrypter = new Blowfish();
			pool.set(encrypter);
		}
		return encrypter;
	}

	public static String encrypt(String str) {
		if (str == null)
			return null;
		try {
			return new String(Base64.encodeBase64(get().encrypt(
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
			return new String(get().decrypt(
					Base64.decodeBase64(str.getBytes("UTF-8"))), "UTF-8");
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return "";
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

	public static String encryptBytesToString(byte[] bytes) {
		try {
			return new String(Base64.encodeBase64(get().encrypt(bytes)),
					"UTF-8");
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return "";
		}
	}

	public static String encryptBytesToString(byte[] bytes, int offset,
			int length) {
		try {
			return new String(Base64.encodeBase64(get().encrypt(bytes, offset,
					length)), "UTF-8");
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return "";
		}
	}

	public static byte[] decryptStringToBytes(String str) {
		try {
			return get().decrypt(Base64.decodeBase64(str.getBytes("UTF-8")));
		} catch (Exception ex) {
			log.error("decrypt exception!", ex);
			return new byte[0];
		}
	}

	public static void main(String... strings) {
		String s = "this is a test";
		s = encrypt(s);
		System.out.println(s);
		s = decrypt(s);
		System.out.println(s);
	}

}
