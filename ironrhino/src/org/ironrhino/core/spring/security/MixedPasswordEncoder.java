package org.ironrhino.core.spring.security;

import org.ironrhino.core.util.CodecUtils;
import org.springframework.security.providers.encoding.BasePasswordEncoder;

public class MixedPasswordEncoder extends BasePasswordEncoder {

	public String encodePassword(String rawPass, Object salt) {
		String saltedPass = mergePasswordAndSalt(rawPass, salt, false);
		return CodecUtils.digest(saltedPass);
	}

	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		String pass1 = "" + encPass;
		String pass2 = encodePassword(rawPass, salt);
		return pass1.equals(pass2);
	}

}