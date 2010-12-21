package org.ironrhino.security.socialauth;

import javax.servlet.http.HttpServletRequest;

public interface AuthProvider extends Comparable<AuthProvider> {

	public String getName();

	public String getLogo();

	public boolean isEnabled();
	
	public boolean isDiscoverable();

	public String getLoginRedirectURL(HttpServletRequest request,
			String redirectURL) throws Exception;

	public Profile getProfile(HttpServletRequest request) throws Exception;

}
