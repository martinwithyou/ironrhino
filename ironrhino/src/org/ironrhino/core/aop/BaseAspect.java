package org.ironrhino.core.aop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.ExpressionUtils;
import org.ironrhino.core.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

public class BaseAspect implements Ordered {

	protected Logger log = LoggerFactory.getLogger(getClass());

	protected int order;

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
		return eval(template, jp, retval, null);
	}

	protected Object eval(String template, JoinPoint jp, Object retval,
			Map<String, Object> ctx) {
		if (template == null)
			return null;
		template = template.trim();
		if (template.length() == 0)
			return "";
		Map<String, Object> context = new HashMap<String, Object>();
		Object[] args = jp.getArgs();
		String[] paramNames = ReflectionUtils.getParameterNames(jp);
		if (paramNames == null) {
			log.warn("Unable to resolve method parameter names for method: "
					+ jp.getStaticPart().getSignature()
					+ ". Debug symbol information is required if you are using parameter names in expressions.");
		} else {
			for (int i = 0; i < args.length; i++)
				context.put(paramNames[i], args[i]);
		}
		if (!context.containsKey("_this"))
			context.put("_this", jp.getThis());
		if (!context.containsKey("target"))
			context.put("target", jp.getTarget());
		if (!context.containsKey("aspect"))
			context.put("aspect", this);
		if (retval != null && !context.containsKey("retval"))
			context.put("retval", retval);
		if (!context.containsKey("args"))
			context.put("args", jp.getArgs());
		if (!context.containsKey("user"))
			context.put("user", AuthzUtils.getUserDetails(UserDetails.class));
		if (ctx != null)
			context.putAll(ctx);
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

	@SuppressWarnings("rawtypes")
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

}
