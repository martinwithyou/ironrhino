package org.ironrhino.core.struts;

import javax.servlet.FilterConfig;

import org.apache.struts2.sitemesh.OldDecorator2NewStrutsFreemarkerDecorator;
import org.apache.struts2.views.freemarker.FreemarkerManager;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Factory;
import com.opensymphony.sitemesh.DecoratorSelector;
import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import com.opensymphony.sitemesh.webapp.SiteMeshWebAppContext;
import com.opensymphony.xwork2.inject.Inject;

public class MyFreeMarkerPageFilter extends SiteMeshFilter {

	@Inject(required = false)
	public static void setFreemarkerManager(FreemarkerManager mgr) {
		OldDecorator2NewStrutsFreemarkerDecorator.setFreemarkerManager(mgr);
	}

	protected FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
		super.init(filterConfig);
	}

	protected DecoratorSelector initDecoratorSelector(
			SiteMeshWebAppContext webAppContext) {
		Factory factory = Factory.getInstance(new Config(filterConfig));
		factory.refresh();
		return new MyFreemarkerMapper2DecoratorSelector(factory
				.getDecoratorMapper());
	}
}
