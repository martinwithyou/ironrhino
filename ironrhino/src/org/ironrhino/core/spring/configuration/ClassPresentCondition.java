package org.ironrhino.core.spring.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

public class ClassPresentCondition implements Condition {

	@Override
	public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
		return matches((String) md.getAnnotationAttributes(
				ClassPresentConditional.class.getName()).get("value"));
	}

	public static boolean matches(String value) {
		if (value == null)
			return true;
		return ClassUtils.isPresent(value,
				ClassPresentCondition.class.getClassLoader());
	}

}
