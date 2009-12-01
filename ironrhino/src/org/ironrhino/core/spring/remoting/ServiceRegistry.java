package org.ironrhino.core.spring.remoting;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.metadata.Remoting;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.core.util.AppInfo;
import org.springframework.context.ApplicationContext;

@Singleton
@Named("serviceRegistry")
public class ServiceRegistry {

	private Log log = LogFactory.getLog(getClass());

	@Inject
	private ApplicationContext ctx;

	// TODO from zookeeper?
	private Map<String, Set<String>> registry = new HashMap<String, Set<String>>();

	private Map<Class, Object> services = new HashMap<Class, Object>();

	public String locate(String serviceName) {
		Set<String> set = getRegistry().get(serviceName);
		if (set == null || set.isEmpty())
			return "localhost";
		return set.iterator().next();
	}

	public void register(String serviceName) {
		String address = AppInfo.getHostAddress();
		Set<String> set = getRegistry().get(serviceName);
		if (set == null) {
			set = new HashSet<String>();
			getRegistry().put(serviceName, set);
		}
		set.add(address);
	}

	public void unregister(String serviceName) {
		String address = AppInfo.getHostAddress();
		Set<String> set = getRegistry().get(serviceName);
		if (set != null)
			set.remove(address);
	}

	public Map<String, Set<String>> getRegistry() {
		return registry;
	}

	public Map<Class, Object> getServices() {
		return services;
	}

	@PostConstruct
	public void init() {
		String[] beanNames = ctx.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			if (StringUtils.isAlphanumeric(beanName)
					&& ctx.isSingleton(beanName)) {
				Object bean = ctx.getBean(beanName);
				Class clazz = bean.getClass();
				if (Proxy.isProxyClass(clazz)
						&& bean.toString().indexOf("remoting") > -1)// client
					continue;
				Class[] interfaces = clazz.getInterfaces();
				if (interfaces != null) {
					for (Class inte : interfaces) {
						Remoting remoting = AnnotationUtils.getAnnotation(inte,
								Remoting.class);
						if (remoting != null) {
							if (StringUtils.isBlank(remoting.name())
									|| remoting.name().equals(beanName)) {
								services.put(inte, bean);
								register(inte.getName());
								log.info("register service :" + inte.getName());
								break;
							}
						}
					}
				}
			}
		}
	}

	@PreDestroy
	public void destroy() throws Exception {
		for (Class clazz : services.keySet())
			unregister(clazz.getName());
	}
}
