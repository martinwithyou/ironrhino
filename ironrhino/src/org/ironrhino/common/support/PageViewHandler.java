package org.ironrhino.common.support;

import java.util.Date;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ironrhino.common.service.PageViewService;
import org.ironrhino.core.servlet.AccessHandler;
import org.ironrhino.core.session.HttpSessionManager;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

@Singleton
@Named
@Order(Integer.MIN_VALUE)
public class PageViewHandler implements AccessHandler {

	@Autowired(required = false)
	private PageViewService pageViewService;

	@Autowired(required = false)
	private ExecutorService executorService;

	@Inject
	private HttpSessionManager httpSessionManager;

	@Override
	public String getPattern() {
		return null;
	}

	@Override
	public boolean handle(final HttpServletRequest request,
			HttpServletResponse response) {
		if (pageViewService != null
				&& request.getMethod().equalsIgnoreCase("GET")
				&& !request.getRequestURI().startsWith("/assets/")
				&& !request.getRequestURI().endsWith("/favicon.ico")) {
			Runnable task = new Runnable() {
				public void run() {
					pageViewService.put(new Date(), RequestUtils
							.getRemoteAddr(request), request.getRequestURL()
							.toString(), httpSessionManager
							.getSessionId(request), RequestUtils
							.getCookieValue(request, "U"), request
							.getHeader("Referer"));
				}
			};
			if (executorService == null)
				task.run();
			else
				executorService.execute(task);
		}
		return false;
	}
}
