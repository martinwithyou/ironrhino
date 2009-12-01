package org.ironrhino.core.spring.remoting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.metadata.Remoting;
import org.ironrhino.core.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class HttpInvokerServer extends HttpInvokerServiceExporter implements
		ApplicationContextAware {

	private Log log = LogFactory.getLog(getClass());

	private ThreadLocal<Class> serviceInterface = new ThreadLocal<Class>();

	private ThreadLocal<Object> service = new ThreadLocal<Object>();

	private Map<Class, Object> proxies = new HashMap<Class, Object>();

	private ApplicationContext ctx;

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		try {
			Class clazz = Class
					.forName(uri.substring(uri.lastIndexOf('/') + 1));
			serviceInterface.set(clazz);
			RemoteInvocation invocation = readRemoteInvocation(request);
			RemoteInvocationResult result = invokeAndCreateResult(invocation,
					getProxyForService()); // getProxy is final cannot override
			writeRemoteInvocationResult(request, response, result);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex
					.getMessage());
		}
	}

	@Override
	public void prepare() {
		String[] beanNames = ctx.getParent().getBeanDefinitionNames();
		for (String beanName : beanNames) {
			if (StringUtils.isAlphanumeric(beanName)
					&& ctx.isSingleton(beanName)) {
				Object bean = ctx.getBean(beanName);
				Class[] interfaces = bean.getClass().getInterfaces();
				if (interfaces != null) {
					for (Class inte : interfaces) {
						Remoting remoting = AnnotationUtils.getAnnotation(inte,
								Remoting.class);
						if (remoting != null) {
							if (StringUtils.isBlank(remoting.name())
									|| remoting.name().equals(beanName)) {
								serviceInterface.set(inte);
								service.set(bean);
								proxies.put(inte, super.getProxyForService());
								log.info("export service :" + inte.getName());
								// TODO register service to service
								// center,zookeeper?
								break;
							}
						}
					}
				}
			}
		}
		serviceInterface.set(null);
		service.set(null);
	}

	@Override
	public Object getService() {
		return service.get();
	}

	@Override
	public Class getServiceInterface() {
		return serviceInterface.get();
	}

	@Override
	protected Object getProxyForService() {
		return proxies.get(getServiceInterface());
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}
}
