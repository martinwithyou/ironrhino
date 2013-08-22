package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Richtable {

	String formid() default "";

	String order() default "";

	boolean showPageSize() default true;

	boolean searchable() default false;

	boolean filterable() default true;

	String actionColumnButtons() default "";

	String bottomButtons() default "";

	String listHeader() default "";

	String listFooter() default "";

	String formHeader() default "";

	String formFooter() default "";

	String rowDynamicAttributes() default "";

	Readonly readonlyConfig() default @Readonly;

}