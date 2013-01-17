package org.ironrhino.core.remoting.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.ironrhino.core.remoting.HessianClient;
import org.ironrhino.core.remoting.HttpInvokerClient;
import org.ironrhino.core.remoting.JsonCallClient;
import org.ironrhino.core.remoting.Remoting;
import org.ironrhino.core.remoting.ServiceRegistry;
import org.ironrhino.core.util.AppInfo;
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

	@SuppressWarnings("unchecked")
	public void init() {
		prepare();
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
			if (!bd.isSingleton())
				continue;
			String beanClassName = bd.getBeanClassName();
			if (beanClassName == null)
				continue;
			Class<?> clazz = null;
			try {
				clazz = Class.forName(beanClassName);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				continue;
			}
			if (clazz.equals(HessianClient.class)
					|| clazz.equals(HttpInvokerClient.class)
					|| clazz.equals(JsonCallClient.class)) {// remoting_client
				String serviceName = (String) bd.getPropertyValues()
						.getPropertyValue("serviceInterface").getValue();
				importServices.put(serviceName, Collections.EMPTY_LIST);
			} else {
				export(clazz, beanName, beanClassName);
			}
		}
		for (String serviceName : exportServices.keySet())
			register(serviceName);
		for (String serviceName : importServices.keySet())
			lookup(serviceName);
		onReady();
	}

	private void export(Class<?> clazz, String beanName, String beanClassName) {
		if (!clazz.isInterface()) {
			Remoting remoting = clazz.getAnnotation(Remoting.class);
			if (remoting != null) {
				Class<?>[] classes = remoting.value();
				if (classes == null || classes.length == 0) {
					log.warn(
							"@Remoting on concrete class [{}] must assign interfaces to export services",
							clazz.getName());
				} else {
					for (Class<?> inte : classes) {
						if (!inte.isInterface()) {
							log.warn(
									"class [{}] in @Remoting on class [{}] must be interface",
									inte.getName(), clazz.getName());
						} else if (!inte.isAssignableFrom(clazz)) {
							log.warn(
									" class [{}] must implements interface [{}] in @Remoting",
									clazz.getName(), inte.getName());
						} else {
							exportServices.put(inte.getName(), beanName);
							log.info(" exported service [{}] for bean [{}#{}]",
									new String[] { inte.getName(), beanName,
											beanClassName });
						}
					}
				}
			}
			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces != null) {
				for (Class<?> inte : interfaces) {
					export(inte, beanName, beanClassName);
				}
			}
		} else {
			Remoting remoting = clazz.getAnnotation(Remoting.class);
			if (remoting != null) {
				exportServices.put(clazz.getName(), beanName);
				log.info(
						" exported service [{}] for bean [{}#{}]",
						new String[] { clazz.getName(), beanName, beanClassName });
			}
			for (Class<?> c : clazz.getInterfaces())
				export(c, beanName, beanClassName);
		}
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

	public void register(String serviceName) {
		String host = AppInfo.getHostAddress();
		doRegister(serviceName, host);
		onRegister(serviceName, host);
	}

	protected void onDiscover(String serviceName, String host) {
		log.info("discovered " + serviceName + "@" + host);
	}

	protected void onRegister(String serviceName, String host) {
		log.info("registered " + serviceName + "@" + host);
	}

	protected void prepare() {

	}

	protected void onReady() {

	}

	protected void lookup(String serviceName) {

	}

	protected void doRegister(String serviceName, String host) {

	}

}
