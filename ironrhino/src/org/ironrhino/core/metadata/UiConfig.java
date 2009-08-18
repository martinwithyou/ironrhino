package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface UiConfig {

	public static final String DEFAULT_TYPE = "input";

	String displayName() default "";

	String type() default DEFAULT_TYPE;

	int size() default 0;

	int displayOrder() default Integer.MAX_VALUE;

	boolean required() default false;

	boolean readonly() default false;

	boolean hide() default false;

}
