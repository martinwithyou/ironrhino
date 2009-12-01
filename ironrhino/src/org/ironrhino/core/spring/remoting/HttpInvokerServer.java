package org.ironrhino.core.spring.remoting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class HttpInvokerServer extends HttpInvokerServiceExporter {

	private Log log = LogFactory.getLog(getClass());

	private ThreadLocal<Class> serviceInterface = new ThreadLocal<Class>();

	private ThreadLocal<Object> service = new ThreadLocal<Object>();

	private Map<Class, Object> proxies = new HashMap<Class, Object>();

	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

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
		for (Map.Entry<Class, Object> entry : serviceRegistry.getServices()
				.entrySet()) {
			serviceInterface.set(entry.getKey());
			service.set(entry.getValue());
			proxies.put(entry.getKey(), super.getProxyForService());
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

}
