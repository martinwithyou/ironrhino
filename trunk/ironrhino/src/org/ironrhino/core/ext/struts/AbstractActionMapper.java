package org.ironrhino.core.ext.struts;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.RequestUtils;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.inject.Inject;

public abstract class AbstractActionMapper implements ActionMapper {

	public static final String ID = "id";

	public static final String REST_STYLE = "restStyle";

	public static final String DATA_TYPE = "dataType";

	protected Log log = LogFactory.getLog(getClass());

	@Inject(StrutsConstants.STRUTS_I18N_ENCODING)
	private String encoding = "UTF-8";

	public ActionMapping getMapping(HttpServletRequest request,
			ConfigurationManager configManager) {
		String uri = getUri(request);
		// this is no action mapping
		if (uri.equals("") || uri.equals("/") || uri.startsWith("/struts/")
				|| uri.startsWith("/cas/"))
			return null;
		return getActionMappingFromRequest(request, uri, configManager
				.getConfiguration());
	}

	public abstract ActionMapping getActionMappingFromRequest(
			HttpServletRequest request, String uri, Configuration config);

	public String getEncoding() {
		return encoding;
	}

	public String getUri(HttpServletRequest request) {
		// handle http dispatcher includes.
		String uri = (String) request
				.getAttribute("javax.servlet.include.servlet_path");
		if (uri == null)
			uri = RequestUtils.getServletPath(request);
		if ("".endsWith(uri))
			uri = "/";
		if (uri != null)
			return uri;
		uri = request.getRequestURI();
		return uri.substring(request.getContextPath().length());
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
