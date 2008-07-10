package org.ironrhino.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface AutoConfig {

	String namespace() default "";

	Class action() default Object.class;

	String[] results() default {};

	String fileupload() default "";

}
