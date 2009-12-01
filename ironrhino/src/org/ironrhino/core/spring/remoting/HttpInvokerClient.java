package org.ironrhino.core.spring.remoting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

public class HttpInvokerClient extends HttpInvokerProxyFactoryBean {

	private static Log log = LogFactory.getLog(HttpInvokerClient.class);

	@Override
	public void afterPropertiesSet() {
		String interfaceName = getServiceInterface().getName();
		String serviceUrl = discoverServiceUrl(interfaceName);
		log.info("discovered service url:" + serviceUrl);
		setServiceUrl(serviceUrl);
		super.afterPropertiesSet();
	}

	protected String discoverServiceUrl(String interfaceName) {
		// TODO auto discover service url from service center,zookeeper?
		String serviceUrl = "http://localhost:8080/ironrhino/remoting/"
				+ interfaceName;
		return serviceUrl;
	}

}
