package org.ironrhino.core.struts;

import java.io.IOException;
import java.util.Locale;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class OverridableConfiguration extends Configuration {

	private OverridableTemplateProvider overridableTemplateProvider;

	public OverridableTemplateProvider getOverridableTemplateProvider() {
		return overridableTemplateProvider;
	}

	public void setOverridableTemplateProvider(
			OverridableTemplateProvider overridableTemplateProvider) {
		if (overridableTemplateProvider != null) {
			this.overridableTemplateProvider = overridableTemplateProvider;
			this.overridableTemplateProvider.setConfiguration(this);
		}
	}

	public Template getTemplate(String name, Locale locale, String encoding,
			boolean parse) throws IOException {
		Template result = null;
		if (overridableTemplateProvider != null)
			result = overridableTemplateProvider.getTemplate(name, locale,
					encoding, parse);
		if (result == null)
			result = super.getTemplate(name, locale, encoding, parse);
		return result;
	}

}
