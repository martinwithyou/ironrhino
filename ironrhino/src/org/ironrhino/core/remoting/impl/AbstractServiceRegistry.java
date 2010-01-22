package org.ironrhino.core.remoting.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.metadata.Remoting;
import org.ironrhino.core.remoting.HessianClient;
import org.ironrhino.core.remoting.HttpInvokerClient;
import org.ironrhino.core.remoting.ServiceRegistry;
import org.ironrhino.core.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public abstract class AbstractServiceRegistry implements ServiceRegistry,
		BeanFactoryPostProcessor {

	ConfigurableListableBeanFactory beanFactory;

	protected Map<String, List<String>> importServices = new ConcurrentHashMap<String, List<String>>();

	protected Map<String, Object> exportServices = new HashMap<String, Object>();

	private boolean converted;

	public Map<String, List<String>> getImportServices() {
		return importServices;
	}

	@Override
	public Map<String, Object> getExportServices() {
		if (!converted) {
			converted = true;
			Map<String, Object> map = new HashMap<String, Object>();
			for (Map.Entry<String, Object> entry : exportServices.entrySet())
				map.put(entry.getKey(), beanFactory.getBean((String) entry
						.getValue()));
			exportServices = map;
		}
		return exportServices;
	}

	public void init() {
		prepare();
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
			if (!bd.isSingleton())
				continue;
			Class clazz = null;
			try {
				clazz = Class.forName(bd.getBeanClassName());
			} catch (Exception e) {
				clazz = Object.class;
			}
			if (clazz.equals(HessianClient.class)
					|| clazz.equals(HttpInvokerClient.class)) {// remoting_client
				String serviceName = (String) bd.getPropertyValues()
						.getPropertyValue("serviceInterface").getValue();
				importServices.put(serviceName, Collections.EMPTY_LIST);
			} else {
				Class[] interfaces = clazz.getInterfaces();
				if (interfaces != null) {
					for (Class inte : interfaces) {
						Remoting remoting = AnnotationUtils.getAnnotation(inte,
								Remoting.class);
						if (remoting != null) {
							if (StringUtils.isBlank(remoting.name())
									|| remoting.name().equals(beanName)) {
								String serviceName = inte.getName();
								exportServices.put(serviceName, beanName);
								break;
							}
						}
					}
				}
			}
		}

		for (String serviceName : exportServices.keySet())
			register(serviceName);
		for (String serviceName : importServices.keySet())
			discover(serviceName);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory arg)
			throws BeansException {
		beanFactory = arg;
		init();
	}

	protected abstract void prepare();

	protected abstract void register(String serviceName);

	protected abstract void discover(String serviceName);

}
