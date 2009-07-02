package org.ironrhino.common.support;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ironrhino.core.ext.struts.AutoConfigResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.SimpleMapModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateHashModelEx;

public class TemplateProvider {

	private String ftlLocation = AutoConfigResult.DEFAULT_FTL_LOCATION;

	private String ftlClasspath = AutoConfigResult.DEFAULT_FTL_CLASSPATH;

	private Configuration configuration;

	@Autowired
	private ResourceLoader resourceLoader;

	private Map allSharedVariables;

	public void setAllSharedVariables(Map allSharedVariables) {
		this.allSharedVariables = allSharedVariables;
	}

	public void setFtlLocation(String ftlLocation) {
		this.ftlLocation = ftlLocation;
	}

	public void setFtlClasspath(String ftlClasspath) {
		this.ftlClasspath = ftlClasspath;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		configuration = new Configuration();
		TemplateLoader ftlLocation = new FileTemplateLoader(resourceLoader
				.getResource(this.ftlLocation).getFile());
		TemplateLoader ftlClasspath = new FileTemplateLoader(resourceLoader
				.getResource("classpath:" + this.ftlClasspath).getFile());
		MultiTemplateLoader templateLoader = new MultiTemplateLoader(
				new TemplateLoader[] { ftlLocation, ftlClasspath });
		configuration.setTemplateLoader(templateLoader);
		DefaultObjectWrapper wrapper = new DefaultObjectWrapper();
		configuration.setObjectWrapper(wrapper);
		configuration.setDefaultEncoding("UTF-8");
		if (allSharedVariables != null && allSharedVariables.size() > 0) {
			TemplateHashModelEx hash = new SimpleMapModel(allSharedVariables,
					wrapper);
			configuration.setAllSharedVariables(hash);
		}
	}

	public Template getTemplate(String templateName) throws IOException {
		return configuration.getTemplate(templateName);
	}

	public Template getTemplate(String arg0, Locale arg1, String arg2,
			boolean arg3) throws IOException {
		return configuration.getTemplate(arg0, arg1, arg2, arg3);
	}

	public Template getTemplate(String arg0, Locale arg1, String arg2)
			throws IOException {
		return configuration.getTemplate(arg0, arg1, arg2);
	}

	public Template getTemplate(String arg0, Locale arg1) throws IOException {
		return configuration.getTemplate(arg0, arg1);
	}

	public Template getTemplate(String arg0, String arg1) throws IOException {
		return configuration.getTemplate(arg0, arg1);
	}

}
