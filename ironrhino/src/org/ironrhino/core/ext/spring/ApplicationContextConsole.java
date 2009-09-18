package org.ironrhino.core.ext.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.stereotype.Component;

@Component("applicationContextConsole")
public class ApplicationContextConsole {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	private ApplicationContext ctx;

	private Map<String, Object> beans;

	@Autowired(required = false)
	private PropertyPlaceholderConfigurer propertyPlaceholderConfigurer;

	private Properties properties;

	@PostConstruct
	public void init() {
		if (propertyPlaceholderConfigurer == null)
			return;
		try {
			Method method = PropertiesLoaderSupport.class.getDeclaredMethod(
					"mergeProperties", new Class[0]);
			method.setAccessible(true);
			properties = (Properties) method.invoke(
					propertyPlaceholderConfigurer, new Object[0]);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Object execute(String expression) throws Exception {
		if (beans == null) {
			beans = new HashMap<String, Object>();
			String[] beanNames = ctx.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				if (StringUtils.isAlphanumeric(beanName)
						&& ctx.isSingleton(beanName))
					beans.put(beanName, ctx.getBean(beanName));
			}
		}
		try {
			return MVEL.eval(expression, beans);
		} catch (Exception e) {
			throw e;
		}
	}

	public String getConfigValue(String key) {
		String value = null;
		if (properties != null)
			value = properties.getProperty(key);
		if (value == null)
			value = System.getProperty(key);
		return value;
	}

}