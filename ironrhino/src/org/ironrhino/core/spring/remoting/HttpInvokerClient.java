package org.ironrhino.core.spring.remoting;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.util.AppInfo;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

public class HttpInvokerClient extends HttpInvokerProxyFactoryBean {

	private static Log log = LogFactory.getLog(HttpInvokerClient.class);

	@Inject
	private ServiceRegistry serviceRegistry;

	private String host;

	private int port = 8080;

	private String contextPath;

	private int maxRetryTimes = 3;

	private boolean urlFromDiscover;

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	@Override
	public void afterPropertiesSet() {
		String interfaceName = getServiceInterface().getName();
		String serviceUrl = getServiceUrl();
		if (serviceUrl == null) {
			serviceUrl = discoverServiceUrl(interfaceName);
			log.info("locate service url:" + serviceUrl);
			setServiceUrl(serviceUrl);
			urlFromDiscover = true;
		}
		super.afterPropertiesSet();
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return invoke(invocation, 0);
	}

	public Object invoke(MethodInvocation invocation, int retryTimes)
			throws Throwable {
		try {
			return super.invoke(invocation);
		} catch (RemoteConnectFailureException e) {
			// retry
			if (retryTimes < maxRetryTimes)
				return invoke(invocation, retryTimes + 1);
			if (urlFromDiscover) {
				String serviceUrl = discoverServiceUrl(getServiceInterface()
						.getName());
				log.info("relocate service url:" + serviceUrl);
				setServiceUrl(serviceUrl);
				return super.invoke(invocation);
			}
			throw e;
		}
	}

	protected String discoverServiceUrl(String interfaceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		if (StringUtils.isBlank(host)) {
			sb.append(serviceRegistry.locate(interfaceName));
		} else {
			sb.append(host);
		}
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
