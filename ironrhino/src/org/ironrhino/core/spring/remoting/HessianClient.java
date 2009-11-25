package org.ironrhino.core.spring.remoting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class HessianClient extends HessianProxyFactoryBean {

	private static Log log = LogFactory.getLog(HessianClient.class);

	private String serviceUrl;

	@Override
	public void afterPropertiesSet() {
		String interfaceName = getServiceInterface().getName();
		serviceUrl = discoverServiceUrl(interfaceName);
		log.info("discovered service url:" + serviceUrl);
		super.afterPropertiesSet();
	}

	@Override
	public String getServiceUrl() {
		return this.serviceUrl;
	}

	protected String discoverServiceUrl(String interfaceName) {
		// TODO auto discover service url from service center,zookeeper?
		serviceUrl = "http://localhost:8080/ironrhino/remoting/"
				+ interfaceName;
		return serviceUrl;
	}

}
