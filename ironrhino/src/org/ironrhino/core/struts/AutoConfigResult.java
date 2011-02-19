package org.ironrhino.core.struts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.ServletDispatcherResult;
import org.apache.struts2.views.freemarker.FreemarkerResult;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.AppInfo.Stage;

import com.opensymphony.module.sitemesh.util.ClassLoaderUtil;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigResult extends FreemarkerResult {

	private static final long serialVersionUID = -2277156996891287055L;

	public static final String DEFAULT_JSP_LOCATION = "/WEB-INF/view/jsp";

	public static final String DEFAULT_FTL_LOCATION = "/WEB-INF/view/ftl";

	public static final String DEFAULT_FTL_CLASSPATH = "/resources/view";

	private static ServletDispatcherResult servletDispatcherResult = new ServletDispatcherResult();

	@Inject(value = "ironrhino.view.jsp.location", required = false)
	private static String jspLocation = DEFAULT_JSP_LOCATION;

	@Inject(value = "ironrhino.view.ftl.location", required = false)
	private static String ftlLocation = DEFAULT_FTL_LOCATION;

	@Inject(value = "ironrhino.view.ftl.classpath", required = false)
	private static String ftlClasspath = DEFAULT_FTL_CLASSPATH;

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		if (invocation.getResultCode().equals(Action.NONE))
			return;
		if (invocation.getResultCode().equals(Action.SUCCESS)
				&& !invocation.getProxy().getMethod().equals("")
				&& !invocation.getProxy().getMethod().equals("execute")) {
			ActionContext ctx = invocation.getInvocationContext();
			HttpServletRequest request = (HttpServletRequest) ctx
					.get(StrutsStatics.HTTP_REQUEST);
			HttpServletResponse response = (HttpServletResponse) ctx
					.get(StrutsStatics.HTTP_RESPONSE);
			String namespace = invocation.getProxy().getNamespace();
			String url = namespace + (namespace.endsWith("/") ? "" : "/")
					+ invocation.getProxy().getActionName();
			response.sendRedirect(request.getContextPath() + url);
			return;
		}
		String finalLocation = conditionalParse(location, invocation);
		if (finalLocation.endsWith(".jsp"))
			servletDispatcherResult.doExecute(finalLocation, invocation);
		else
			doExecute(finalLocation, invocation);
	}

	private static Map<String, String> cache = new ConcurrentHashMap<String, String>(
			250);

	@Override
	protected String conditionalParse(String param, ActionInvocation invocation) {
		String result = invocation.getResultCode();
		String namespace = invocation.getProxy().getNamespace();
		String actionName = invocation.getInvocationContext().getName();
		if (namespace.equals("/"))
			namespace = "";
		String templateName = getTemplateName(namespace, actionName, result);
		String location = cache.get(templateName);
		if (location == null || AppInfo.getStage() == AppInfo.Stage.DEVELOPMENT) {
			ServletContext context = ServletActionContext.getServletContext();
			URL url = null;
			location = getTemplateLocation(templateName);
			if (location == null) {
				location = new StringBuilder().append(jspLocation).append(
						"/meta/result/").append(result).append(".jsp")
						.toString();
				try {
					url = context.getResource(location);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				if (url == null) {
					location = new StringBuilder().append(ftlLocation).append(
							"/meta/result/").append(result).append(".ftl")
							.toString();
					try {
						url = context.getResource(location);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				if (url == null)
					location = new StringBuilder().append(ftlClasspath).append(
							"/meta/result/").append(result).append(".ftl")
							.toString();
			}
			if (AppInfo.getStage() == Stage.PRODUCTION)
				cache.put(templateName, location);
		}
		return location;
	}

	public static String getTemplateLocation(String templateName) {
		String location = cache.get(templateName);
		if (location == null || AppInfo.getStage() == AppInfo.Stage.DEVELOPMENT) {
			ServletContext context = ServletActionContext.getServletContext();
			URL url = null;
			location = new StringBuilder().append(jspLocation).append(
					templateName).append(".jsp").toString();
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if (url == null) {
				location = new StringBuilder().append(ftlLocation).append(
						templateName).append(".ftl").toString();
				try {
					url = context.getResource(location);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			if (url == null) {
				location = new StringBuilder().append(ftlClasspath).append(
						templateName).append(".ftl").toString();
				url = ClassLoaderUtil.getResource(location.substring(1),
						AutoConfigResult.class);
			}
			if (url == null)
				location = "";
			if (AppInfo.getStage() == Stage.PRODUCTION)
				cache.put(templateName, location);
		}
		return StringUtils.isEmpty(location) ? null : location;
	}

	private String getTemplateName(String namespace, String actionName,
			String result) {
		StringBuilder sb = new StringBuilder();
		sb.append(namespace).append('/').append(actionName);
		if (!result.equals(Action.SUCCESS))
			sb.append('_').append(result);
		return sb.toString();
	}
}
