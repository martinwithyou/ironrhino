package org.ironrhino.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface CheckCache {
	// javascript expression in ${}
	// like "product_code_${args[0].code}"
	String value();
	
	String namespace() default "methodCache";
}
