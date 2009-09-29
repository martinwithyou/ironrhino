package org.ironrhino.core.util;

import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ApplicationContextUtils {

	private static ApplicationContext applicationContext;

	public static ApplicationContext getApplicationContext() {
		if (applicationContext == null)
			applicationContext = WebApplicationContextUtils
					.getWebApplicationContext(ServletActionContext
							.getServletContext());
		return applicationContext;
	}

	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

}