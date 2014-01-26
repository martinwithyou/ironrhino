package org.ironrhino.core.spring.configuration;

import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.AppInfo.Stage;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class StageCondition implements Condition {

	@Override
	public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
		return matches((Stage) md.getAnnotationAttributes(
				StageConditional.class.getName()).get("value"));
	}

	public static boolean matches(Stage stage) {
		if (stage == null)
			return true;
		return AppInfo.getStage() == stage;
	}
}
