package org.ironrhino.core.remoting;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.metadata.PostPropertiesReset;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.util.AppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class HessianClient extends HessianProxyFactoryBean {

	private static Logger log = LoggerFactory.getLogger(HessianClient.class);

	private ServiceRegistry serviceRegistry;

	private ExecutorService executorService;

	private String host;

	private int port = 8080;

	private String contextPath;

	private String version;

	private int maxRetryTimes = 3;

	private List<String> asyncMethods;

	private boolean urlFromDiscovery;

	private boolean discovered; // for lazy discover from serviceRegistry

	private boolean reset;

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void setServiceUrl(String serviceUrl) {
		super.setServiceUrl(serviceUrl);
		reset = true;
	}

	@Override
	public void afterPropertiesSet() {
		String serviceUrl = getServiceUrl();
		if (serviceUrl == null) {
			setServiceUrl("http://fakeurl/");
			reset = false;
			discovered = false;
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
		if (urlFromDiscovery && !discovered) {
			String serviceUrl = discoverServiceUrl(getServiceInterface()
					.getName());
			setServiceUrl(serviceUrl);
			reset();
			discovered = true;
		}

		if (executorService != null && asyncMethods != null) {
			String name = invocation.getMethod().getName();
			if (asyncMethods.contains(name)) {
				executorService.execute(new Runnable() {

					public void run() {
						try {
							invoke(invocation, maxRetryTimes);
						} catch (Throwable e) {
							log.error(e.getMessage(), e);
						}
					}
				});
				return null;
			}
		}
		return invoke(invocation, maxRetryTimes);
	}

	public Object invoke(MethodInvocation invocation, int retryTimes)
			throws Throwable {
		retryTimes--;
		try {
			return super.invoke(invocation);
		} catch (RemoteAccessException e) {
			if (retryTimes < 0)
				throw e;
			if (urlFromDiscovery) {
				String serviceUrl = discoverServiceUrl(getServiceInterface()
						.getName());
				if (!serviceUrl.equals(getServiceUrl())) {
					setServiceUrl(serviceUrl);
					log.info("relocate service url:" + serviceUrl);
					reset();
				}
			}
			return invoke(invocation, retryTimes);
		}
	}

	protected String discoverServiceUrl(String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		if (StringUtils.isBlank(host)) {
			if (serviceRegistry != null) {
				String ho = serviceRegistry.discover(serviceName);
				if (ho != null) {
					sb.append(ho);
					log.info("discovered " + serviceName + "@" + ho);
				} else {
					sb.append("localhost");
					log.error("couldn't discover service:" + serviceName);
				}
			} else {
				sb.append("localhost");
			}
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
		sb.append(serviceName);
		boolean first = true;
		if (StringUtils.isNotBlank(version)) {
			if (first) {
				sb.append('?');
				first = false;
			} else {
				sb.append('&');
			}
			sb.append(Context.VERSION);
			sb.append('=');
			sb.append(version);
		}
		if (AppInfo.getStage() == AppInfo.Stage.PRODUCTION && port == 80)
			try {
				if (first) {
					sb.append('?');
					first = false;
				} else {
					sb.append('&');
				}
				sb.append(Context.KEY);
				sb.append('=');
				sb.append(URLEncoder.encode(Blowfish.encrypt(serviceName),
						"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		return sb.toString();
	}
}
