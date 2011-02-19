package org.ironrhino.core.struts;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;

public abstract class AbstractActionMapper implements ActionMapper {

	public static final String ID = "id";

	protected Logger log = LoggerFactory.getLogger(getClass());

	public static String getUri(HttpServletRequest request) {
		// handle http dispatcher includes.
		String uri = (String) request
				.getAttribute("javax.servlet.include.servlet_path");
		if (uri == null) {
			uri = request.getRequestURI();
			uri = uri.substring(request.getContextPath().length());
		}
		// uri = org.apache.struts2.RequestUtils.getServletPath(request);
		if (uri.equals("/") || "".equals(uri))
			return "/index";
		return uri;
	}

	public ActionMapping getMappingFromActionName(String actionName) {
		ActionMapping mapping = new ActionMapping();
		mapping.setName(actionName);
		return parseActionName(mapping);
	}

	protected ActionMapping parseActionName(ActionMapping mapping) {
		if (mapping.getName() == null) {
			return mapping;
		}
		// handle "name!method" convention.
		String name = mapping.getName();
		int exclamation = name.lastIndexOf("!");
		if (exclamation != -1) {
			mapping.setName(name.substring(0, exclamation));
			mapping.setMethod(name.substring(exclamation + 1));
		}
		return mapping;
	}

}
