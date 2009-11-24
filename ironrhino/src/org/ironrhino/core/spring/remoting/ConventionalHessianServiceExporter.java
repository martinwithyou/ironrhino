package org.ironrhino.core.spring.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.caucho.HessianServiceExporter;

import com.caucho.hessian.server.HessianSkeleton;

public class ConventionalHessianServiceExporter extends HessianServiceExporter
		implements ApplicationContextAware {

	private Log log = LogFactory.getLog(getClass());

	private ThreadLocal<Class> serviceInterface = new ThreadLocal<Class>();

	private ThreadLocal<Object> service = new ThreadLocal<Object>();

	private Map<Class, HessianSkeleton> skeletons = new HashMap<Class, HessianSkeleton>();

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
				String message = "please add @Remoting on " + clazz;
				log.error(message);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
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
								skeletons.put(inte, new HessianSkeleton(
										getProxyForService(),
										getServiceInterface()));
								log.info("export service :" + inte.getName());
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
