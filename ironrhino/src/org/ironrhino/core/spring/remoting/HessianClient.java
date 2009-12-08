package org.ironrhino.core.spring.remoting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.metadata.PostPropertiesReset;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.util.AppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class HessianClient extends HessianProxyFactoryBean {

	private static Log log = LogFactory.getLog(HessianClient.class);

	@Inject
	private ServiceRegistry serviceRegistry;

	@Autowired(required = false)
	@Qualifier("cachedThreadPool")
	private ExecutorService cachedThreadPool;

	private String host;

	private int port = 8080;

	private String contextPath;

	private int maxRetryTimes = 3;

	private List<String> asyncMethods;

	private boolean urlFromDiscovery;

	private boolean reset;

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

	public void setAsyncMethods(String asyncMethods) {
		if (StringUtils.isNotBlank(asyncMethods)) {
			asyncMethods = asyncMethods.trim();
			String[] array = asyncMethods.split(",");
			this.asyncMethods = Arrays.asList(array);
		}
	}

	@Override
	public void setServiceUrl(String serviceUrl) {
		super.setServiceUrl(serviceUrl);
		reset = true;
	}

	@Override
	public void afterPropertiesSet() {
		String interfaceName = getServiceInterface().getName();
		String serviceUrl = getServiceUrl();
		if (serviceUrl == null) {
			serviceUrl = discoverServiceUrl(interfaceName);
			log.info("locate service url:" + serviceUrl);
			setServiceUrl(serviceUrl);
			reset = false;
			urlFromDiscovery = true;
		}
		setHessian2(true);
		super.afterPropertiesSet();
	}

	@PostPropertiesReset
	public void reset() throws IOException {
		if (reset) {
			reset = false;
			super.afterPropertiesSet();
		}
	}

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		if (cachedThreadPool != null && asyncMethods != null) {
			String name = invocation.getMethod().getName();
			if (asyncMethods.contains(name)) {
				cachedThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							invoke(invocation, 0);
						} catch (Throwable e) {
							log.error(e.getMessage(), e);
						}
					}
				});
				return null;
			}
		}
		return invoke(invocation, 0);
	}

	public Object invoke(MethodInvocation invocation, int retryTimes)
			throws Throwable {
		try {
			return super.invoke(invocation);
		} catch (RemoteAccessException e) {
			// retry
			if (retryTimes < maxRetryTimes - 1)
				return invoke(invocation, retryTimes + 1);
			if (urlFromDiscovery) {
				String serviceUrl = discoverServiceUrl(getServiceInterface()
						.getName());
				if (!serviceUrl.equals(getServiceUrl())) {
					setServiceUrl(serviceUrl);
					log.info("relocate service url:" + serviceUrl);
					reset();
					return super.invoke(invocation);
				}
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
		sb.append("/remoting/hessian/");
		sb.append(interfaceName);
		if (AppInfo.getStage() == AppInfo.Stage.PRODUCTION && port == 80)
			sb.append("?" + Blowfish.encrypt(interfaceName));
		return sb.toString();
	}
}
