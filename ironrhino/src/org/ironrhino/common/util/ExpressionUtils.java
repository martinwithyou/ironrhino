package org.ironrhino.common.util;

import java.util.Map;

import org.mvel2.MVEL;

public class ExpressionUtils {

	public static Object eval(String expression, Map<String, Object> context) {
		return MVEL.eval(expression, context);
	}

	public static String render(String template, Map<String, Object> context) {
		int begin = 0, start = template.indexOf("${"), end = template.indexOf(
				"}", start);
		StringBuilder sb = new StringBuilder();
		while (end > 0) {
			sb.append(template.substring(begin, start));
			String ex = template.substring(start + 2, end);
			sb.append(eval(ex, context));
			begin = end + 1;
			start = template.indexOf("${", begin);
			if (start < 0)
				break;
			end = template.indexOf("}", begin);
		}
		if (end < template.length() - 1)
			sb.append(template.substring(begin));
		return sb.toString();
	}

}
