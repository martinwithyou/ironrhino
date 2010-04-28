package org.ironrhino.core.security.csrf;

import javax.servlet.http.HttpServletRequest;

public interface CsrfManager {

	public static final String KEY_CSRF = "csrf";

	public String createToken(HttpServletRequest request);

	public boolean validateToken(HttpServletRequest request);

}
