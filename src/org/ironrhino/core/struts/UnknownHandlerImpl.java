package org.ironrhino.core.struts;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.UnknownHandler;
import com.opensymphony.xwork2.XWorkException;
import com.opensymphony.xwork2.config.entities.ActionConfig;

public class UnknownHandlerImpl implements UnknownHandler {

	public ActionConfig handleUnknownAction(String namespace, String actionName)
			throws XWorkException {
		//TODO handleUnkownAction
		return null;
	}

	public Object handleUnknownActionMethod(Object action, String methodName)
			throws NoSuchMethodException {
		return null;
	}

	public Result handleUnknownResult(ActionContext actionContext,
			String actionName, ActionConfig actionConfig, String resultCode)
			throws XWorkException {
		return null;
	}

}
