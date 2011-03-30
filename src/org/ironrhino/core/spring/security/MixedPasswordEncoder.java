package org.ironrhino.core.spring.security;

import org.ironrhino.core.util.CodecUtils;
import org.springframework.security.authentication.encoding.BasePasswordEncoder;

public class MixedPasswordEncoder extends BasePasswordEncoder {

	public String encodePassword(String rawPass, Object salt) {
		String saltedPass = mergePasswordAndSalt(rawPass, salt, false);
		return CodecUtils.digest(saltedPass);
	}

	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		if (encPass == null)
			encPass = "";
		if (rawPass == null)
			rawPass = "";
		rawPass = encodePassword(rawPass, salt);
		return encPass.equals(rawPass);
	}

}