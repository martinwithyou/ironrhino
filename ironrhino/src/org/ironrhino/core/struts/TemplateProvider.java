package org.ironrhino.core.struts;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerManager;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Container;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.SimpleMapModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModelEx;

public class TemplateProvider {

	private Log log = LogFactory.getLog(this.getClass());

	private String ftlLocation = AutoConfigResult.DEFAULT_FTL_LOCATION;

	private String ftlClasspath = AutoConfigResult.DEFAULT_FTL_CLASSPATH;

	private Configuration configuration;

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

	private Configuration getConfiguration() {
		if (configuration == null) {
			try {
				Container con = ActionContext.getContext().getContainer();
				FreemarkerManager freemarkerManager = con
						.getInstance(org.apache.struts2.views.freemarker.FreemarkerManager.class);
				configuration = freemarkerManager
						.getConfiguration(ServletActionContext
								.getServletContext());
				TemplateHashModelEx hash = new SimpleMapModel(
						allSharedVariables, BeansWrapper.getDefaultInstance());
				configuration.setAllSharedVariables(hash);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return configuration;
	}

	public Template getTemplate(String templateName) throws IOException {
		Locale loc = getConfiguration().getLocale();
		return getTemplate(templateName, loc, getConfiguration().getEncoding(
				loc), true);
	}

	public Template getTemplate(String templateName, Locale locale,
			String encoding) throws IOException {
		return getTemplate(templateName, locale, encoding, true);
	}

	public Template getTemplate(String templateName, Locale locale)
			throws IOException {
		return getTemplate(templateName, locale, getConfiguration()
				.getEncoding(locale), true);
	}

	public Template getTemplate(String templateName, String encoding)
			throws IOException {
		return getTemplate(templateName, getConfiguration().getLocale(),
				encoding, true);
	}

	public Template getTemplate(String templateName, Locale arg1, String arg2,
			boolean arg3) throws IOException {
		if (templateName.startsWith(ftlLocation)
				|| templateName.startsWith(ftlClasspath))
			return getConfiguration().getTemplate(templateName, arg1, arg2,
					arg3);
		String name = ftlLocation + (templateName.indexOf('/') != 0 ? "/" : "")
				+ templateName;
		Template t = getConfiguration().getTemplate(name, arg1, arg2, arg3);
		if (t == null) {
			name = ftlClasspath + (templateName.indexOf('/') != 0 ? "/" : "")
					+ templateName;
			t = getConfiguration().getTemplate(name, arg1, arg2, arg3);
		}
		return t;
	}

}
