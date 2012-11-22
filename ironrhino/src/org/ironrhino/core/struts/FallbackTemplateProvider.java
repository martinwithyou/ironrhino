package org.ironrhino.core.struts;

import java.io.IOException;
import java.util.Locale;

import freemarker.template.Template;

public interface FallbackTemplateProvider {

	public Template getTemplate(String templateName, Locale locale,
			String encoding, boolean parse) throws IOException;

}
