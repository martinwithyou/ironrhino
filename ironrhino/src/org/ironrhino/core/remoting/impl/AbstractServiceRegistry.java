package org.ironrhino.core.remoting.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.remoting.HessianClient;
import org.ironrhino.core.remoting.HttpInvokerClient;
import org.ironrhino.core.remoting.Remoting;
import org.ironrhino.core.remoting.ServiceRegistry;
import org.ironrhino.core.util.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public abstract class AbstractServiceRegistry implements ServiceRegistry,
		BeanFactoryPostProcessor {

	protected Logger log = LoggerFactory.getLogger(getClass());

	ConfigurableListableBeanFactory beanFactory;

	protected Map<String, List<String>> importServices = new ConcurrentHashMap<String, List<String>>();

	protected Map<String, Object> exportServices = new HashMap<String, Object>();

	private Random random = new Random();

	private boolean converted;

	public Map<String, List<String>> getImportServices() {
		return importServices;
	}

	public Map<String, Object> getExportServices() {
		if (!converted) {
			converted = true;
			Map<String, Object> map = new HashMap<String, Object>();
			for (Map.Entry<String, Object> entry : exportServices.entrySet())
				map.put(entry.getKey(),
						beanFactory.getBean((String) entry.getValue()));
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
			lookup(serviceName);
		onReady();
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory arg)
			throws BeansException {
		beanFactory = arg;
		init();
	}

	public String discover(String serviceName) {
		List<String> hosts = getImportServices().get(serviceName);
		if (hosts != null && hosts.size() > 0) {
			String host = hosts.get(random.nextInt(hosts.size()));
			onDiscover(serviceName, host);
			return host;
		} else {
			return null;
		}

	}

	protected void onDiscover(String serviceName, String host) {

	}

	protected void onRegister(String serviceName, String host) {
		log.info("registered " + serviceName + "@" + host);
	}

	protected abstract void prepare();

	protected abstract void onReady();

	protected abstract void lookup(String serviceName);

}
