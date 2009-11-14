package org.ironrhino.core.struts;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.sitemesh.OldDecorator2NewStrutsFreemarkerDecorator;
import org.ironrhino.core.util.HtmlUtils;

import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.sitemesh.Content;
import com.opensymphony.xwork2.ActionContext;

public class MyOldDecorator2NewStrutsFreemarkerDecorator extends
		OldDecorator2NewStrutsFreemarkerDecorator {

	public static final String X_FRAGMENT = "X-Fragment";

	public MyOldDecorator2NewStrutsFreemarkerDecorator(Decorator oldDecorator) {
		super(oldDecorator);
	}

	protected void render(Content content, HttpServletRequest request,
			HttpServletResponse response, ServletContext servletContext,
			ActionContext ctx) throws ServletException, IOException {
		String replacement = request.getHeader(X_FRAGMENT);
		if (StringUtils.isNotBlank(replacement)) {
			try {
				StringWriter writer = new StringWriter();
				content.writeBody(writer);
				response.getWriter().write(
						HtmlUtils.compress(replacement.split(","), writer
								.toString()));
				return;
			} catch (Exception e) {

			}
		} else {
			super.render(content, request, response, servletContext, ctx);
		}

	}

}
