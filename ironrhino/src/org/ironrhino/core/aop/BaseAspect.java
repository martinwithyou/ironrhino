package org.ironrhino.core.aop;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.mvel2.templates.TemplateRuntime;
import org.springframework.core.Ordered;

public class BaseAspect implements Ordered {

	private int order;

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
		Object value = TemplateRuntime.eval(template, context);
		return value;
	}

	protected boolean isBypass() {
		return AopContext.isBypass(this.getClass());
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
