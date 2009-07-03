package org.ironrhino.core.ext.struts;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import com.opensymphony.xwork2.inject.Inject;

import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.SimpleMapModel;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModelEx;

public class RichFreemarkerManager extends
		org.apache.struts2.views.freemarker.FreemarkerManager {

	private Log log = LogFactory.getLog(this.getClass());

	private String ftlLocation = AutoConfigResult.DEFAULT_FTL_LOCATION;

	private String ftlClasspath = AutoConfigResult.DEFAULT_FTL_CLASSPATH;

	private Configuration configuration;

	@Inject(value = "ironrhino.view.ftl.location", required = false)
	public void setFtlLocation(String val) {
		this.ftlLocation = val;
	}

	@Inject(value = "ironrhino.view.ftl.classpath", required = false)
	public void setFtlClasspath(String val) {
		this.ftlClasspath = val;
	}

	protected freemarker.template.Configuration createConfiguration(
			ServletContext servletContext) throws TemplateException {
		configuration = super.createConfiguration(servletContext);
		Map globalVariables = new HashMap();
		globalVariables.put("statics", BeansWrapper.getDefaultInstance()
				.getStaticModels());
		TemplateHashModelEx hash = new SimpleMapModel(globalVariables,
				BeansWrapper.getDefaultInstance());
		configuration.setAllSharedVariables(hash);
		configuration.setDateFormat("yyyy-MM-dd");
		configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
		configuration.setNumberFormat("0.##");
		configuration.setURLEscapingCharset("UTF-8");
		configuration
				.setTemplateExceptionHandler(new TemplateExceptionHandler() {
					public void handleTemplateException(TemplateException ex,
							Environment env, Writer writer)
							throws TemplateException {
						log.error(ex.getMessage());
					}
				});
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		ServletContextResourcePatternResolver servletContextResourcePatternResolver = new ServletContextResourcePatternResolver(
				servletContext);
		Resource[] resources;
		String searchPath;
		String location;
		String namespace;
		try {
			searchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ ftlClasspath + "/import/*.ftl";
			resources = resourcePatternResolver.getResources(searchPath);
			for (Resource r : resources) {
				location = r.getURL().toString();
				namespace = location.substring(location.lastIndexOf('/') + 1);
				namespace = namespace.substring(0, namespace.indexOf('.'));
				configuration.addAutoImport(namespace, location
						.substring(location.indexOf(ftlClasspath)));
			}
		} catch (IOException e) {
			log.info(e.getMessage());
		}
		try {
			searchPath = ftlLocation + "/import/*.ftl";
			resources = servletContextResourcePatternResolver
					.getResources(searchPath);
			for (Resource r : resources) {
				location = r.getURL().toString();
				namespace = location.substring(location.lastIndexOf('/') + 1);
				namespace = namespace.substring(0, namespace.indexOf('.'));
				configuration.addAutoImport(namespace, location
						.substring(location.indexOf(ftlLocation)));
			}
		} catch (IOException e) {
			log.info(e.getMessage());
		}
		try {
			searchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ ftlClasspath + "/include/*.ftl";
			resources = resourcePatternResolver.getResources(searchPath);
			for (Resource r : resources) {
				location = r.getURL().toString();
				configuration.addAutoInclude(location.substring(location
						.indexOf(ftlClasspath)));
			}
		} catch (IOException e) {
			log.info(e.getMessage());
		}
		try {
			searchPath = ftlLocation + "/include/*.ftl";
			resources = servletContextResourcePatternResolver
					.getResources(searchPath);
			for (Resource r : resources) {
				location = r.getURL().toString();
				configuration.addAutoInclude(location.substring(location
						.indexOf(ftlLocation)));
			}
		} catch (IOException e) {
			log.info(e.getMessage());
		}

		return configuration;
	}

	public Configuration getConfiguration() {
		return configuration;
	}
}
