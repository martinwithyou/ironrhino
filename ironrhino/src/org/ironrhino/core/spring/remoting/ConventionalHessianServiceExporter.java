package org.ironrhino.core.spring.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.metadata.Remoting;
import org.ironrhino.core.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.caucho.HessianServiceExporter;

import com.caucho.hessian.server.HessianSkeleton;

public class ConventionalHessianServiceExporter extends HessianServiceExporter
		implements ApplicationContextAware {

	private Log log = LogFactory.getLog(getClass());

	private ThreadLocal<Class> serviceInterface = new ThreadLocal<Class>();

	private Map<Class, HessianSkeleton> skeletons = new ConcurrentHashMap<Class, HessianSkeleton>();

	// @Autowired //doesn't works
	private ApplicationContext ctx;

	@Override
	@Autowired
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		try {
			Class clazz = Class
					.forName(uri.substring(uri.lastIndexOf('/') + 1));
			serviceInterface.set(clazz);
			HessianSkeleton skeleton = skeletons.get(clazz);
			if (skeleton == null) {
				Remoting remoting = AnnotationUtils.getAnnotation(clazz,
						Remoting.class);
				if (remoting == null) {
					String message = "please add @Remoting on " + clazz;
					log.error(message);
					response.sendError(HttpServletResponse.SC_NOT_FOUND,
							message);
				}
				skeleton = new HessianSkeleton(getProxyForService(),
						getServiceInterface());
				skeletons.put(clazz, skeleton);
			}
			super.handleRequest(request, response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ex
					.getMessage());
		}
	}

	@Override
	public void prepare() {
		// override and do nothing
	}

	@Override
	public Object getService() {
		Class clazz = getServiceInterface();
		Remoting remoting = AnnotationUtils
				.getAnnotation(clazz, Remoting.class);
		if (remoting == null)
			return null;
		if (StringUtils.isNotBlank(remoting.name()))
			return ctx.getBean(remoting.name(), clazz);
		else
			return ctx.getBean(clazz);
	}

	@Override
	public Class getServiceInterface() {
		return serviceInterface.get();
	}

	public void invoke(InputStream inputStream, OutputStream outputStream)
			throws Throwable {
		doInvoke(skeletons.get(getServiceInterface()), inputStream,
				outputStream);
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}
}
