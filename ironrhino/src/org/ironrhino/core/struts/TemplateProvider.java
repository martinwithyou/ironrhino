package org.ironrhino.core.struts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.springframework.beans.factory.annotation.Value;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Container;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Singleton
@Named
public class TemplateProvider {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${ironrhino.view.ftl.location:"
			+ AutoConfigResult.DEFAULT_FTL_LOCATION + "}")
	private String ftlLocation;

	@Value("${ironrhino.view.ftl.classpath:"
			+ AutoConfigResult.DEFAULT_FTL_CLASSPATH + "}")
	private String ftlClasspath;

	@Value("${base:}")
	private String base;

	@Value("${assetsBase:}")
	private String assetsBase;

	@Value("${ssoServerBase:}")
	private String ssoServerBase;

	private Configuration configuration;

	public String getFtlLocation() {
		return org.ironrhino.core.util.StringUtils.trimTailSlash(ftlLocation);
	}

	public String getFtlClasspath() {
		return org.ironrhino.core.util.StringUtils.trimTailSlash(ftlClasspath);
	}

	public Map getAllSharedVariables() {
		Map<String, String> allSharedVariables = new HashMap<String, String>();
		if (StringUtils.isNotBlank(base))
			allSharedVariables.put("base",
					org.ironrhino.core.util.StringUtils.trimTailSlash(base));
		if (StringUtils.isNotBlank(assetsBase))
			allSharedVariables.put("assetsBase",
					org.ironrhino.core.util.StringUtils
							.trimTailSlash(assetsBase));
		if (StringUtils.isNotBlank(ssoServerBase))
			allSharedVariables.put("ssoServerBase",
					org.ironrhino.core.util.StringUtils
							.trimTailSlash(ssoServerBase));
		return allSharedVariables;
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
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return configuration;
	}

	public Template getTemplate(String templateName) throws IOException {
		Locale loc = getConfiguration().getLocale();
		return getTemplate(templateName, loc,
				getConfiguration().getEncoding(loc), true);
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
		Template t = null;
		try {
			t = getConfiguration().getTemplate(name, arg1, arg2, arg3);
		} catch (FileNotFoundException e) {
			if (t == null) {
				name = ftlClasspath
						+ (templateName.indexOf('/') != 0 ? "/" : "")
						+ templateName;
				t = getConfiguration().getTemplate(name, arg1, arg2, arg3);
			}
		}
		return t;
	}

}
