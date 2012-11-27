package org.ironrhino.common.action;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

@AutoConfig(namespace = "/")
public class ErrorAction extends BaseAction {

	private static final long serialVersionUID = 7684824080798968019L;

	public String handle() {
		int errorcode = 404;
		try {
			errorcode = Integer.valueOf(getUid());
		} catch (Exception e) {

		}
		String result;
		switch (errorcode) {
		case 403:
			result = ACCESSDENIED;
			break;
		case 404:
			result = NOTFOUND;
			break;
		case 500:
			result = ERROR;
			break;
		default:
			result = NOTFOUND;
		}
		return result;
	}
}
