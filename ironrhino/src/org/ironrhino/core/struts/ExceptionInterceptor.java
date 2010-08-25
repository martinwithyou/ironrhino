package org.ironrhino.core.struts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class ExceptionInterceptor extends AbstractInterceptor {

	protected static final Log log = LogFactory
			.getLog(ExceptionInterceptor.class);

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		String result;
		try {
			result = invocation.invoke();
		} catch (Exception e) {
			Object action = invocation.getAction();
			if (action instanceof ValidationAware) {
				ValidationAware validationAwareAction = (ValidationAware) action;
				validationAwareAction.addActionError(e.getMessage());
			}
			log.error(e.getMessage(), e);
			result = BaseAction.ERROR;
		}
		return result;
	}

}
