package org.ironrhino.core.spring.security.password;

import org.ironrhino.core.util.CodecUtils;

public class V1PasswordEncoder extends VersionedPasswordEncoder {

	public String encodePassword(String rawPass, Object salt) {
		return CodecUtils.digest(rawPass);
	}

}
