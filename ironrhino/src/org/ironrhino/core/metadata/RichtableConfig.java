package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface RichtableConfig {

	boolean showPageSize() default true;

	String actionColumnButtons() default "";

	String bottomButtons() default "";

	String listHeader() default "";

	String listFooter() default "";

	String rowDynamicAttributes() default "";

}