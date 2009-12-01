package org.ironrhino.core.spring.remoting;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.util.AppInfo;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

public class HttpInvokerClient extends HttpInvokerProxyFactoryBean {

	private static Log log = LogFactory.getLog(HttpInvokerClient.class);

	@Inject
	private ServiceRegistry serviceRegistry;

	private int port = 8080;

	private String contextPath;

	public void setPort(int port) {
		this.port = port;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public void afterPropertiesSet() {
		String interfaceName = getServiceInterface().getName();
		String serviceUrl = getServiceUrl();
		if (serviceUrl == null) {
			serviceUrl = discoverServiceUrl(interfaceName);
			log.info("locate service url:" + serviceUrl);
			setServiceUrl(serviceUrl);
		}
		super.afterPropertiesSet();
	}

	protected String discoverServiceUrl(String interfaceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(serviceRegistry.locate(interfaceName));
		if (port != 80) {
			sb.append(':');
			sb.append(port);
		}
		if (StringUtils.isNotBlank(contextPath))
			sb.append(contextPath);
		sb.append("/remoting/httpinvoker/");
		sb.append(interfaceName);
		if (AppInfo.getStage() == AppInfo.Stage.PRODUCTION && port == 80) {
			sb.append("?" + Blowfish.encrypt(interfaceName));
		}
		return sb.toString();
	}

}
