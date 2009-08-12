package org.ironrhino.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface FormElement {

	public static final String DEFAULT_TYPE = "input";

	String type() default DEFAULT_TYPE;

	int size() default 0;

	int displayOrder() default Integer.MAX_VALUE;

	boolean required() default false;

	boolean readonly() default false;

}
