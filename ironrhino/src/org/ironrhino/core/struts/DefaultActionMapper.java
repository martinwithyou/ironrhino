package org.ironrhino.core.struts;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.ironrhino.core.model.ResultPage;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.entities.PackageConfig;

public class DefaultActionMapper extends AbstractActionMapper {

	public String getUriFromActionMapping(ActionMapping mapping) {
		StringBuilder sb = new StringBuilder();
		String namespace = mapping.getNamespace();
		namespace = "".equals(namespace) ? "/" : namespace;
		sb.append(namespace);
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/')
			sb.append("/");
		sb.append(mapping.getName());
		String method = mapping.getMethod();
		if (method != null)
			sb.append("/" + method);
		Map params = mapping.getParams();
		try {
			if (method != null && params != null && params.containsKey(ID))
				sb.append("/"
						+ URLEncoder.encode((String) params.get(ID),
								getEncoding()));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return sb.toString();
	}

	@Override
	public ActionMapping getActionMappingFromRequest(
			HttpServletRequest request, String uri, Configuration config) {
		// if have a extension it is normal request
		if (!uri.contains(getCmsPath())
				&& uri.lastIndexOf('.') > uri.lastIndexOf('/'))
			return null;
		if (uri.startsWith(getCmsPath())) {
			String pageId = uri.substring(getCmsPath().length() - 1);
			ActionMapping mapping = new ActionMapping();
			mapping.setNamespace("/common");
			mapping.setName("displayPage");
			Map<String, Object> params = new HashMap<String, Object>(3);
			params.put(ID, pageId);
			mapping.setParams(params);
			return mapping;
		}
		String namespace = null;
		String name = null;
		String methodAndUid = null;
		// Find the longest matching namespace and name
		uri = uri.replace("//", "/");
		if (uri.indexOf(';') > 0)
			uri = uri.substring(0, uri.indexOf(';'));
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
					name = org.ironrhino.core.util.StringUtils
							.toCamelCase(array[0]);
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
		name = org.ironrhino.core.util.StringUtils.toCamelCase(arr[0]);
		if (arr.length > 1)
			methodAndUid = arr[1];

		ActionMapping mapping = new ActionMapping();
		mapping.setNamespace(namespace);
		mapping.setName(org.ironrhino.core.util.StringUtils.toCamelCase(name));
		Map<String, Object> params = new HashMap<String, Object>(3);
		// process resultPage.pageNo and resultPage.pageSize
		String pn = request.getParameter(ResultPage.PAGENO_PARAM_NAME);
		if (StringUtils.isNumeric(pn))
			params.put("resultPage.pageNo", pn);
		String ps = request.getParameter(ResultPage.PAGESIZE_PARAM_NAME);
		if (StringUtils.isNumeric(ps))
			params.put("resultPage.pageSize", ps);
		if (StringUtils.isNotBlank(methodAndUid)) {
			String uid = null;
			String[] array = StringUtils.split(methodAndUid, "/", 2);
			mapping.setMethod(array[0]);
			if (array.length > 1) {
				uid = array[1];
			}

			if (StringUtils.isNotBlank(uid)) {
				try {
					params.put(ID, URLDecoder.decode(uid, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		if (params.size() > 0)
			mapping.setParams(params);
		return mapping;
	}

}
