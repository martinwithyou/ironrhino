package org.ironrhino.core.ext.struts;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.ironrhino.common.util.JsonUtils;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.metadata.Redirect;
import org.springframework.beans.BeanUtils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.inject.Inject;

public class JsonResult implements Result {

	private String encoding = "UTF-8";

	protected ActionMapper actionMapper;

	@Inject(StrutsConstants.STRUTS_I18N_ENCODING)
	public void setEncoding(String val) {
		encoding = val;
	}

	@Inject
	public void setActionMapper(ActionMapper mapper) {
		this.actionMapper = mapper;
	}

	public String generateJson(ActionInvocation invocation) {
		Object action = invocation.getAction();
		Method method = BeanUtils.findDeclaredMethod(action.getClass(),
				invocation.getProxy().getMethod(), null);
		JsonConfig annotation = method.getAnnotation(JsonConfig.class);
		if (annotation != null && StringUtils.isNotBlank(annotation.root())) {
			Object value = invocation.getStack().findValue(annotation.root());
			return JsonUtils.toJson(value, annotation.serializer());
		}

		boolean hasErrors = false;
		Map<String, Object> map = new HashMap<String, Object>();
		if (action instanceof ValidationAware) {
			ValidationAware validationAwareAction = (ValidationAware) action;
			if (validationAwareAction.hasErrors()) {
				hasErrors = true;
				if (validationAwareAction.hasActionErrors()) {
					map.put("actionErrors", validationAwareAction
							.getActionErrors());
				}
				if (validationAwareAction.hasFieldErrors()) {
					map.put("fieldErrors", validationAwareAction
							.getFieldErrors());
				}
				return JsonUtils.toJson(map);
			} else {
				map.put("hasErrors", false);
			}
			if (validationAwareAction.hasActionMessages()) {
				map.put("hasActionMessages", true);
				map.put("actionMessages", validationAwareAction
						.getActionMessages());
			} else {
				map.put("hasActionMessages", false);
			}
		}
		if (!hasErrors) {
			Redirect redirect = method.getAnnotation(Redirect.class);
			if (redirect != null) {
				// see org.apache.struts2.dispatcher.ServletRedirectResult
				String targetUrl = String.valueOf(invocation.getStack()
						.findValue(redirect.targetUrl()));
				if (targetUrl == null)
					targetUrl = "";
				if (targetUrl.indexOf(':') == -1) {
					HttpServletRequest request = ServletActionContext
							.getRequest();
					if (!targetUrl.startsWith("/")) {
						ActionMapping mapping = actionMapper.getMapping(
								request, Dispatcher.getInstance()
										.getConfigurationManager());
						String namespace = null;
						if (mapping != null) {
							namespace = mapping.getNamespace();
						}
						if ((namespace != null) && (namespace.length() > 0)
								&& (!"/".equals(namespace))) {
							targetUrl = namespace + "/" + targetUrl;
						} else {
							targetUrl = "/" + targetUrl;
						}
					}
					targetUrl = RequestUtils.getBaseUrl(request) + targetUrl;
				}
				targetUrl = ServletActionContext.getResponse()
						.encodeRedirectURL(targetUrl);
				ServletActionContext.getResponse().setHeader("X-Redirect-To",
						targetUrl);
			} else {
				if (annotation == null || annotation.propertyName() == null
						|| annotation.propertyName().length == 0) {
					return JsonUtils.toJson(map);
				}
				String[] propertyNameArray = annotation.propertyName();
				if (propertyNameArray != null && propertyNameArray.length > 0) {
					for (String name : propertyNameArray) {
						Object value = invocation.getStack().findValue(name);
						if (value != null)
							map.put(name, value);
					}
				}
			}
		}
		return JsonUtils.toJson(map);
	}

	public void execute(ActionInvocation invocation) throws Exception {
		String json = generateJson(invocation);
		HttpServletResponse response = ServletActionContext.getResponse();
		// response.setContentType("application/json;charset=" + getEncoding());
		response.setContentType("text/javascript;charset=" + encoding);
		if (!response.containsHeader("Cache-Control")) {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
		}
		PrintWriter out = response.getWriter();
		out.print(json);
		out.flush();
		out.close();
	}
}