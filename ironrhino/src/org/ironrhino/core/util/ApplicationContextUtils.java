package org.ironrhino.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.service.EntityManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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

	@SuppressWarnings("unchecked")
	public static <T extends Persistable<?>> BaseManager<T> getEntityManager(
			Class<T> entityClass) {
		String entityManagerName = StringUtils.uncapitalize(entityClass
				.getSimpleName()) + "Manager";
		try {
			return (BaseManager<T>) getApplicationContext().getBean(
					entityManagerName);
		} catch (NoSuchBeanDefinitionException e) {
			EntityManager<T> entityManager = ApplicationContextUtils
					.getBean(EntityManager.class);
			entityManager.setEntityClass(entityClass);
			return entityManager;
		}
	}

}