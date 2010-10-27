package org.ironrhino.core.aop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.ironrhino.core.spring.ApplicationContextConsole;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.ExpressionUtils;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

public class BaseAspect implements Ordered {

	protected Logger log = LoggerFactory.getLogger(getClass());

	protected int order;

	@Inject
	private ApplicationContextConsole applicationContextConsole;

	protected boolean isBypass() {
		return AopContext.isBypass(this.getClass());
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	protected Object eval(String template, JoinPoint jp, Object retval) {
		if (template == null)
			return null;
		template = template.trim();
		if (template.length() == 0)
			return "";

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("_this", jp.getThis());
		context.put("target", jp.getTarget());
		context.put("aspect", this);
		if (retval != null)
			context.put("retval", retval);
		context.put("args", jp.getArgs());
		context.put("user", AuthzUtils.getUserDetails(UserDetails.class));
		return ExpressionUtils.eval(template, context);
	}

	protected Object eval(String template, JoinPoint jp) {
		return eval(template, jp, null);
	}

	protected String evalString(String template, JoinPoint jp, Object retval) {
		try {
			Object obj = eval(template, jp, retval);
			if (obj == null)
				return null;
			return obj.toString();
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return template;
		}
	}

	protected boolean evalBoolean(String template, JoinPoint jp, Object retval) {
		try {
			if (StringUtils.isBlank(template))
				return true;
			return Boolean.parseBoolean(evalString(template, jp, retval));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	protected int evalInt(String template, JoinPoint jp, Object retval) {
		try {
			Object obj = eval(template, jp, retval);
			if (obj == null)
				return 0;
			if (obj instanceof Integer)
				return (Integer) obj;
			return Integer.parseInt(obj.toString());
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return 0;
		}

	}

	protected long evalLong(String template, JoinPoint jp, Object retval) {
		try {
			Object obj = eval(template, jp, retval);
			if (obj == null)
				return 0;
			if (obj instanceof Long)
				return (Long) obj;
			return Long.parseLong(obj.toString());
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return 0;
		}

	}

	protected double evalDouble(String template, JoinPoint jp, Object retval) {
		try {
			Object obj = eval(template, jp, retval);
			if (obj == null)
				return 0;
			if (obj instanceof Double)
				return (Double) obj;
			return Double.parseDouble(obj.toString());
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return 0;
		}

	}

	protected List evalList(String template, JoinPoint jp, Object retval) {
		try {
			Object obj = eval(template, jp, retval);
			if (obj == null)
				return null;
			if (obj instanceof List)
				return (List) obj;
			return Arrays.asList(obj.toString().split(","));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean config(String key, boolean _default) {
		String value = applicationContextConsole.getConfigValue(key);
		return StringUtils.isNotBlank(value) ? Boolean.valueOf(value)
				: _default;
	}

	public String config(String key, String _default) {
		String value = applicationContextConsole.getConfigValue(key);
		return StringUtils.isNotBlank(value) ? value : _default;
	}

	public long config(String key, long _default) {
		String value = applicationContextConsole.getConfigValue(key);
		return StringUtils.isNotBlank(value) ? Long.valueOf(value) : _default;
	}

	public int config(String key, int _default) {
		String value = applicationContextConsole.getConfigValue(key);
		return StringUtils.isNotBlank(value) ? Integer.valueOf(value)
				: _default;
	}

	public double config(String key, double _default) {
		String value = applicationContextConsole.getConfigValue(key);
		return StringUtils.isNotBlank(value) ? Double.valueOf(value) : _default;
	}

}
