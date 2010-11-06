package org.ironrhino.core.session;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public interface HttpSessionManager extends HttpSessionStore {

	public String getSessionTracker(WrappedHttpSession session);

	public String getSessionTrackerName();
	
	public boolean supportSessionTrackerFromURL();
	
	public String getLocaleCookieName();
	
	public Locale getLocale(HttpServletRequest request);
	

}
