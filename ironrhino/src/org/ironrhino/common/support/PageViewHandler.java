package org.ironrhino.common.support;

import java.util.Date;

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

	@Inject
	private HttpSessionManager httpSessionManager;

	@Override
	public String getPattern() {
		return null;
	}

	@Override
	public boolean handle(HttpServletRequest request,
			HttpServletResponse response) {
		if (pageViewService != null
				&& request.getMethod().equalsIgnoreCase("GET")) {
			pageViewService.put(new Date(),
					RequestUtils.getRemoteAddr(request), request
							.getRequestURL().toString(), httpSessionManager
							.getSessionId(request), request
							.getHeader("Referer"));
		}
		return false;
	}

}
