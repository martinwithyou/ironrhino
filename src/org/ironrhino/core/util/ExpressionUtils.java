package org.ironrhino.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.mvel2.MVEL;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

public class ExpressionUtils {

	private static Map<String, CompiledTemplate> templateCache = new ConcurrentHashMap<String, CompiledTemplate>();

	private static Map<String, CompiledExpression> expressionCache = new ConcurrentHashMap<String, CompiledExpression>();

	public static Object evalExpression(String expression,
			Map<String, ?> context) {
		if (StringUtils.isBlank(expression))
			return expression;

		CompiledExpression ce = expressionCache.get(expression);
		if (ce == null) {
			ce = new ExpressionCompiler(expression).compile();
			expressionCache.put(expression, ce);
		}
		return MVEL.executeExpression(ce, context);
	}

	public static Object eval(String template, Map<String, ?> context) {
		if (StringUtils.isBlank(template))
			return template;
		CompiledTemplate ct = templateCache.get(template);
		if (ct == null) {
			ct = new TemplateCompiler(template).compile();
			templateCache.put(template, ct);
		}
		return TemplateRuntime.execute(ct, context);
	}

}
