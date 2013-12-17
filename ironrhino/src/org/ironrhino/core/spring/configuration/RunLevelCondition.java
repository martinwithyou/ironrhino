package org.ironrhino.core.spring.configuration;

import java.util.Map;

import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.AppInfo.RunLevel;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RunLevelCondition implements Condition {

	@Override
	public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
		Map<String, Object> attributes = md
				.getAnnotationAttributes(RunLevelConditional.class.getName());
		return AppInfo.matchesRunLevel((RunLevel) attributes.get("value"));
	}

}
