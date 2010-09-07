package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.RequestUtils;

@AutoConfig(namespace = "/")
public class LocaleAction extends BaseAction {

	public static final String SETTING_KEY_AVAILABLE_LOCALES = "availableLocales";

	private String lang;

	private Locale[] availableLocales;

	@Inject
	private SettingControl settingControl;

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Locale[] getAvailableLocales() {
		return availableLocales;
	}

	@Override
	public String execute() {
		if (lang != null) {
			HttpServletRequest request = ServletActionContext.getRequest();
			HttpServletResponse response = ServletActionContext.getResponse();
			Locale locale = null;
			if (StringUtils.isBlank(lang)) {
				locale = null;
			} else {
				for (Locale var : Locale.getAvailableLocales()) {
					if (lang.equalsIgnoreCase(var.toString())) {
						locale = var;
						break;
					}
				}
			}
			RequestUtils.setLocale(request, response, locale);
			targetUrl = "/";
			return REDIRECT;
		}
		availableLocales = Locale.getAvailableLocales();
		String[] locales = settingControl
				.getStringArray(SETTING_KEY_AVAILABLE_LOCALES);
		if (locales != null && locales.length > 0) {
			List<String> _locales = Arrays.asList(locales);
			List<Locale> list = new ArrayList<Locale>(locales.length);
			for (Locale locale : availableLocales) {
				if (_locales.contains(locale.toString()))
					list.add(locale);
			}
			availableLocales = list.toArray(new Locale[0]);
		}
		return SUCCESS;
	}

}
