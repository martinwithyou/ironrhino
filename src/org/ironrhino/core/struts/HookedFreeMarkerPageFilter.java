package org.ironrhino.core.struts;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.sitemesh.FreeMarkerPageFilter;
import org.ironrhino.core.util.HtmlUtils;

import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.xwork2.ActionContext;

public class HookedFreeMarkerPageFilter extends FreeMarkerPageFilter {

	protected void applyDecorator(Page page, Decorator decorator,
			HttpServletRequest req, HttpServletResponse res,
			ServletContext servletContext, ActionContext ctx)
			throws ServletException, IOException {
		String replacement = req.getParameter("_replacement_");
		if (StringUtils.isNotBlank(replacement)) {
			compressBody(page, decorator, req, res, servletContext, ctx);
		} else {
			super
					.applyDecorator(page, decorator, req, res, servletContext,
							ctx);
		}
	}

	protected void compressBody(Page page, Decorator decorator,
			HttpServletRequest req, HttpServletResponse res,
			ServletContext servletContext, ActionContext ctx) {
		String body = page.getBody();
		try {
			String compressed = HtmlUtils.compress(req.getParameter(
					"_replacement_").split(","), body);
			if (compressed.length() > 0)
				body = compressed;
			res.getWriter().append(body);
		} catch (Exception e) {
			// not important exception,no need to log it
			e.printStackTrace();
		}
	}

}
