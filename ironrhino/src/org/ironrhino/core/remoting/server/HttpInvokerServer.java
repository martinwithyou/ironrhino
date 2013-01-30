package org.ironrhino.core.remoting.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ironrhino.core.remoting.Context;
import org.ironrhino.core.remoting.ServiceRegistry;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.util.AppInfo;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class HttpInvokerServer extends HttpInvokerServiceExporter {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static ThreadLocal<Object> service = new ThreadLocal<Object>();

	private Map<Class<?>, Object> proxies = new HashMap<Class<?>, Object>();

	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> map = request.getParameterMap();
		if (map != null && map.size() > 0)
			Context.PARAMETERS_MAP.set(map);
		String uri = request.getRequestURI();
		try {
			String interfaceName = uri.substring(uri.lastIndexOf('/') + 1);
			if (AppInfo.getStage() == AppInfo.Stage.PRODUCTION
					&& request.getServerPort() == 80
					&& request.getAttribute("_OAUTH_REQUEST") == null) {
				String s = Blowfish.decrypt(Context.get(Context.KEY));
				if (!interfaceName.equals(s)) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			}
			Class<?> clazz = Class.forName(interfaceName);
			Context.SERVICE.set(clazz);
			RemoteInvocation invocation = readRemoteInvocation(request);
			Object proxy = getProxyForService();
			if (proxy != null) {
				RemoteInvocationResult result = invokeAndCreateResult(
						invocation, proxy); // getProxy is final cannot override
				writeRemoteInvocationResult(request, response, result);
			} else {
				String msg = "No Service:" + getServiceInterface().getName();
				log.error("No Service:" + getServiceInterface());
				response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getMessage());
		} finally {
			Context.reset();
		}
	}

	@Override
	public void prepare() {
		if (serviceRegistry != null) {
			for (Map.Entry<String, Object> entry : serviceRegistry
					.getExportServices().entrySet()) {
				try {
					Class<?> intf = Class.forName(entry.getKey());
					Context.SERVICE.set(intf);
					service.set(entry.getValue());
					proxies.put(intf, super.getProxyForService());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			Context.SERVICE.remove();
			service.remove();
		}
	}

	@Override
	public Object getService() {
		return service.get();
	}

	@Override
	public Class<?> getServiceInterface() {
		return Context.SERVICE.get();
	}

	@Override
	protected Object getProxyForService() {
		return proxies.get(getServiceInterface());
	}

}
