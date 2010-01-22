package org.ironrhino.core.remoting.impl;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.metadata.Remoting;
import org.ironrhino.core.remoting.ServiceRegistry;
import org.ironrhino.core.util.AnnotationUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

@Singleton
@Named("serviceRegistry")
public abstract class AbstractServiceRegistry implements ServiceRegistry {

	@Inject
	protected ApplicationContext ctx;

	protected Map<String, List<String>> importServices = new ConcurrentHashMap<String, List<String>>();

	protected Map<String, Object> exportServices = new HashMap<String, Object>();

	public Map<String, List<String>> getImportServices() {
		return importServices;
	}

	@Override
	public Map<String, Object> getExportServices() {
		return exportServices;
	}

	@PostConstruct
	public void init() {
		prepare();
		String[] beanNames = ctx.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			if (StringUtils.isAlphanumeric(beanName)
					&& ctx.isSingleton(beanName)) {
				Object bean = ctx.getBean(beanName);
				Class clazz = bean.getClass();
				if ((Proxy.isProxyClass(clazz) || AopUtils.isAopProxy(bean))
						&& bean.toString().indexOf("remoting") > -1) {// remoting_client
					String serviceName = null;
					if (bean instanceof Advised) {
						serviceName = ((Advised) bean).getProxiedInterfaces()[0]
								.getName();

					} else if (Proxy.isProxyClass(bean.getClass())) {
						serviceName = bean.getClass().getInterfaces()[0]
								.getName();
					}
					if (serviceName != null)
						importServices.put(serviceName, Collections.EMPTY_LIST);
					continue;
				}
				Class[] interfaces = clazz.getInterfaces();
				if (interfaces != null) {
					for (Class inte : interfaces) {
						Remoting remoting = AnnotationUtils.getAnnotation(inte,
								Remoting.class);
						if (remoting != null) {
							if (StringUtils.isBlank(remoting.name())
									|| remoting.name().equals(beanName)) {
								String serviceName = inte.getName();
								exportServices.put(serviceName, bean);
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

	protected abstract void prepare();

	protected abstract void register(String serviceName);

	protected abstract void discover(String serviceName);

}
