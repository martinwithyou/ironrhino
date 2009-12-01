package org.ironrhino.core.spring.remoting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class HessianClient extends HessianProxyFactoryBean {

	private static Log log = LogFactory.getLog(HessianClient.class);

	@Override
	public void afterPropertiesSet() {
		String interfaceName = getServiceInterface().getName();
		String serviceUrl = getServiceUrl();
		if (serviceUrl == null) {
			serviceUrl = discoverServiceUrl(interfaceName);
			log.info("discovered service url:" + serviceUrl);
			setServiceUrl(serviceUrl);
		}
		setHessian2(true);
		super.afterPropertiesSet();
	}

	protected String discoverServiceUrl(String interfaceName) {
		// TODO auto discover service url from service center,zookeeper?
		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:8080/ironrhino");
		sb.append("/remoting/hessian/");
		sb.append(interfaceName);
		return sb.toString();
	}

}
