package org.ironrhino.core.ext.struts;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.dispatcher.mapper.ActionMapping;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;

public class DelegateActionMapper extends AbstractActionMapper {

	private AbstractActionMapper defaultActionMapper;

	private AbstractActionMapper restfulActionMapper;

	private String restPrefix = "/rest/";

	public DelegateActionMapper() {
		defaultActionMapper = new DefaultActionMapper();
		restfulActionMapper = new RestfulActionMapper();
	}

	@Override
	public ActionMapping getMapping(HttpServletRequest request,
			ConfigurationManager configManager) {
		defaultActionMapper.setCmsPath(getCmsPath());
		String uri = getUri(request);
		// this is no action mapping
		if (uri.equals("") || uri.equals("/") || uri.startsWith("/struts/")
				|| uri.startsWith("/cas/"))
			return null;
		if (uri.startsWith(restPrefix)) {
			uri = uri.substring(restPrefix.length() - 1);
			return restfulActionMapper.getActionMappingFromRequest(request,
					uri, configManager.getConfiguration());
		}
		return getActionMappingFromRequest(request, uri, configManager
				.getConfiguration());
	}

	public String getUriFromActionMapping(ActionMapping mapping) {
		return defaultActionMapper.getUriFromActionMapping(mapping);
	}

	@Override
	public ActionMapping getActionMappingFromRequest(
			HttpServletRequest request, String uri, Configuration config) {
		return defaultActionMapper.getActionMappingFromRequest(request, uri,
				config);
	}

}
