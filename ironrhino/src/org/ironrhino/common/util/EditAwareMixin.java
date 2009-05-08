package org.ironrhino.common.util;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

public class EditAwareMixin extends DelegatingIntroductionInterceptor implements
		EditAware {

	private transient Map map = new HashMap();

	public Object getOldValue(String propertyName) {
		return map.get(propertyName);
	}

	public boolean isEdited(String propertyName) {
		return map.containsKey(propertyName);
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (invocation.getMethod().getName().indexOf("set") == 0) {
			Object _this = invocation.getThis();
			String propertyName = invocation.getMethod().getName().substring(3);
			propertyName = StringUtils.uncapitalize(propertyName);
			Object oldValue = PropertyUtils.getProperty(_this, propertyName);
			Object newValue = invocation.getArguments()[0];
			if (!isEquals(oldValue, newValue))
				map.put(propertyName, oldValue);
		}
		return super.invoke(invocation);
	}

	public static boolean isEquals(Object oldValue, Object newValue) {
		if (oldValue == null && newValue == null)
			return true;
		if (oldValue != null)
			return oldValue.equals(newValue);
		else
			return newValue.equals(oldValue);
	}
}
