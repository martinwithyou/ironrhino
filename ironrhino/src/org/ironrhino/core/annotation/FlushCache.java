package org.ironrhino.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface FlushCache {
	// javascript expression in ${},split by comma
	// like "product_code_${args[0].code},product_id_${args[0].id}"
	String value();

	String namespace() default "methodCache";
}
