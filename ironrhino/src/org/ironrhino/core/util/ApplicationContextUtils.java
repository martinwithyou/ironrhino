package org.ironrhino.core.util;

import org.apache.struts2.ServletActionContext;
import org.springframework.beans.BeansException;
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

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		try {
			return (T) getApplicationContext().getBean(name);
		} catch (BeansException e) {
			return null;
		}
	}

	public static <T> T getBean(Class<T> t) {
		try {
			return getApplicationContext().getBean(t);
		} catch (BeansException e) {
			return null;
		}
	}

}