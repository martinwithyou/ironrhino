package org.ironrhino.core.spring.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.caucho.HessianServiceExporter;

import com.caucho.hessian.server.HessianSkeleton;

public class HessianServer extends HessianServiceExporter {

	private Log log = LogFactory.getLog(getClass());

	private ThreadLocal<Class> serviceInterface = new ThreadLocal<Class>();

	private ThreadLocal<Object> service = new ThreadLocal<Object>();

	private Map<Class, HessianSkeleton> skeletons = new HashMap<Class, HessianSkeleton>();

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
			super.handleRequest(request, response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex
					.getMessage());
		}
	}

	@Override
	public void invoke(InputStream inputStream, OutputStream outputStream)
			throws Throwable {
		doInvoke(skeletons.get(getServiceInterface()), inputStream,
				outputStream);
	}

	@Override
	public void prepare() {
		for (Map.Entry<Class, Object> entry : serviceRegistry.getServices()
				.entrySet()) {
			serviceInterface.set(entry.getKey());
			service.set(entry.getValue());
			skeletons.put(entry.getKey(), new HessianSkeleton(
					getProxyForService(), getServiceInterface()));
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

}
