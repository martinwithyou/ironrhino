package org.ironrhino.core.spring.configuration;

import java.util.Map;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ResourcePresentCondition implements Condition {

	@Override
	public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata metadata) {
		Map<String, Object> attributes = metadata
				.getAnnotationAttributes(ResourcePresentConditional.class
						.getName());
		return matches((String) attributes.get("value"),
				(Boolean) attributes.get("negated"));
	}

	public static boolean matches(String value, boolean negated) {
		if (value.startsWith("/"))
			value = value.substring(1);
		boolean b = ResourcePresentCondition.class.getClassLoader()
				.getResource(value) != null;
		return b && !negated || !b && negated;
	}

}
