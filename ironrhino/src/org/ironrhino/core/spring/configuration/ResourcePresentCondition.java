package org.ironrhino.core.spring.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ResourcePresentCondition implements Condition {

	@Override
	public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
		return matches((String) md.getAnnotationAttributes(
				ResourcePresentConditional.class.getName()).get("value"));
	}

	public static boolean matches(String value) {
		if (value == null)
			return true;
		return ResourcePresentCondition.class.getClassLoader().getResource(
				value) != null;
	}

}
