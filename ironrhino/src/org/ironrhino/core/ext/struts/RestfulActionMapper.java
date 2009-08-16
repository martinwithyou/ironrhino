package org.ironrhino.core.ext.struts;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.mapper.ActionMapping;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.entities.PackageConfig;

public class RestfulActionMapper extends AbstractActionMapper {

	@Override
	public ActionMapping getMapping(HttpServletRequest request,
			ConfigurationManager configManager) {
		String uri = getUri(request);
		// this is no action mapping
		if (uri.equals("") || uri.equals("/") || uri.startsWith("/struts/"))
			return null;
		// if have a extension and this file exists, it is normal request
		if (uri.lastIndexOf('.') > uri.lastIndexOf('/')
				&& new File(request.getSession().getServletContext()
						.getRealPath(uri)).exists())
			return null;
		return getActionMappingFromRequest(request, uri, configManager
				.getConfiguration());
	}

	public String getUriFromActionMapping(ActionMapping mapping) {
		StringBuilder sb = new StringBuilder();
		String namespace = mapping.getNamespace();
		namespace = "".equals(namespace) ? "/" : namespace;
		sb.append(namespace);
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/')
			sb.append("/");
		sb.append(mapping.getName());
		String method = mapping.getMethod();
		Map params = mapping.getParams();
		try {
			if (params != null && params.containsKey(ID))
				sb.append("/"
						+ URLEncoder.encode((String) params.get(ID),
								getEncoding()));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		if (method != null)
			sb.append(";" + method);
		return sb.toString();
	}

	@Override
	public ActionMapping getActionMappingFromRequest(
			HttpServletRequest request, String uri, Configuration config) {
		String namespace = null;
		String name = null;
		String id = null;
		String dataType = null;
		String method = null;
		uri = uri.replace("//", "/");
		if (!uri.endsWith(";") && uri.indexOf(';') > 0) {
			method = uri.substring(uri.indexOf(';') + 1);
			uri = uri.substring(0, uri.indexOf(';'));
		}
		if (!uri.endsWith(".") && uri.indexOf('.') > 0) {
			dataType = uri.substring(uri.indexOf('.') + 1);
			uri = uri.substring(0, uri.indexOf('.'));
		}
		// Find the longest matching namespace and name

		for (Object var : config.getPackageConfigs().values()) {
			PackageConfig pc = (PackageConfig) var;
			String ns = pc.getNamespace();
			if (!uri.equals(ns) && uri.startsWith(ns)) {
				if (namespace == null
						|| (namespace != null && ns.length() >= namespace
								.length())) {
					String temp = uri.substring(ns.length());
					if ("".equals(temp) || "/".equals(temp))
						continue;
					String[] array = StringUtils.split(temp, "/", 2);
					name = array[0];
					if (pc.getActionConfigs().containsKey(name))
						namespace = ns;
				}
			}
		}

		if (namespace == null) {
			return null;
		}

		String str = uri.substring(namespace.length());
		String[] arr = StringUtils.split(str, "/", 2);
		name = arr[0];
		if (arr.length > 1)
			id = arr[1];
		ActionMapping mapping = new ActionMapping();
		mapping.setNamespace(namespace);
		mapping.setName(name);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put(REST_STYLE, "true");
		if (StringUtils.isNotBlank(id)) {
			try {
				params.put(ID, URLDecoder.decode(id, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if (dataType != null)
			params.put(DATA_TYPE, dataType);
		mapping.setParams(params);
		if (method != null)
			mapping.setMethod(method);
		else
			mapping
					.setMethod(getRestMethod(request, StringUtils
							.isNotBlank(id)));
		return mapping;
	}

	private String getRestMethod(HttpServletRequest request, boolean hasId) {
		String httpMethod = request.getMethod().toUpperCase();
		if (httpMethod.equals("GET")) {
			if (hasId)
				return "view";
			else
				return "list";
		}
		if (httpMethod.equals("POST"))
			return "create";
		else if (httpMethod.equals("PUT"))
			return "update";
		else if (httpMethod.equals("DELETE"))
			return "delete";
		else
			return null;
	}

}
