package org.ironrhino.core.struts;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public class I18N {

	public static String getText(String key) {
		return LocalizedTextUtil.findText(I18N.class, key, ActionContext
				.getContext().getLocale(), key, null);
	}

}
