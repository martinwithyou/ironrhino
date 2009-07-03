package org.ironrhino.core.ext.struts;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.ServletDispatcherResult;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import com.opensymphony.module.sitemesh.util.ClassLoaderUtil;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigResult extends FreemarkerResult {

	public static final String DEFAULT_JSP_LOCATION = "/WEB-INF/view/jsp";

	public static final String DEFAULT_FTL_LOCATION = "/WEB-INF/view/ftl";

	public static final String DEFAULT_FTL_CLASSPATH = "/resources/view";

	private static ServletDispatcherResult servletDispatcherResult = new ServletDispatcherResult();

	private String jspLocation = DEFAULT_JSP_LOCATION;

	private String ftlLocation = DEFAULT_FTL_LOCATION;

	private String ftlClasspath = DEFAULT_FTL_CLASSPATH;

	@Inject(value = "ironrhino.view.jsp.location", required = false)
	public void setJspLocation(String val) {
		this.jspLocation = val;
	}

	@Inject(value = "ironrhino.view.ftl.location", required = false)
	public void setFtlLocation(String val) {
		this.ftlLocation = val;
	}

	@Inject(value = "ironrhino.view.ftl.classpath", required = false)
	public void setFtlClasspath(String val) {
		this.ftlClasspath = val;
	}

	public void execute(ActionInvocation invocation) throws Exception {
		if (invocation.getResultCode().equals(Action.SUCCESS)
				&& !invocation.getProxy().getMethod().equals("")
				&& !invocation.getProxy().getMethod().equals("execute")) {
			ActionContext ctx = invocation.getInvocationContext();
			HttpServletRequest request = (HttpServletRequest) ctx
					.get(ServletActionContext.HTTP_REQUEST);
			HttpServletResponse response = (HttpServletResponse) ctx
					.get(ServletActionContext.HTTP_RESPONSE);
			String namespace = invocation.getProxy().getNamespace();
			String url = namespace + (namespace.endsWith("/") ? "" : "/")
					+ invocation.getProxy().getActionName();
			response.sendRedirect(request.getContextPath() + url);
		}
		String finalLocation = conditionalParse(location, invocation);
		if (finalLocation.endsWith(".jsp"))
			servletDispatcherResult.doExecute(finalLocation, invocation);
		else
			doExecute(finalLocation, invocation);
	}

	protected String conditionalParse(String param, ActionInvocation invocation) {
		String result = invocation.getResultCode();
		String namespace = invocation.getProxy().getNamespace();
		String actionName = invocation.getProxy().getActionName();
		if (namespace.equals("/"))
			namespace = "";
		String location = jspLocation + namespace + "/"
				+ getTemplateName(actionName, result) + ".jsp";
		ServletContext context = ServletActionContext.getServletContext();
		URL url = null;
		try {
			url = context.getResource(location);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null) {
			location = ftlLocation + namespace + "/"
					+ getTemplateName(actionName, result) + ".ftl";
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (url == null) {
			location = ftlClasspath + namespace + "/"
					+ getTemplateName(actionName, result) + ".ftl";
			url = ClassLoaderUtil.getResource(location.substring(1),
					AutoConfigResult.class);
		}
		if (url == null) {
			location = jspLocation + "/" + result + ".jsp";
			try {
				url = context.getResource(location);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (url == null)
			location = ftlClasspath + "/" + result + ".ftl";
		return location;
	}

	private String getTemplateName(String actionName, String resultName) {
		if (resultName.equals(Action.SUCCESS))
			return actionName;
		return actionName + "_" + resultName;
	}
}
