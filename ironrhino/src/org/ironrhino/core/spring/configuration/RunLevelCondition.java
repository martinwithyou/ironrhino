package org.ironrhino.core.spring.configuration;

import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.AppInfo.RunLevel;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RunLevelCondition implements Condition {

	@Override
	public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
		return matches((RunLevel) md.getAnnotationAttributes(
				RunLevelConditional.class.getName()).get("value"));
	}

	public static boolean matches(RunLevel runLevel) {
		if (runLevel == null)
			return true;
		return AppInfo.getRunLevel().compareTo(runLevel) >= 0;
	}

}
