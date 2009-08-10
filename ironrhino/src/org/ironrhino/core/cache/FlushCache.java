package org.ironrhino.core.cache;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface FlushCache {

	// mvel expression "product_code_${args[0].code},product_id_${args[0].id}"
	String key();

	// mvel expression
	String name() default CacheContext.DEFAULT_CACHE_NAME;

	// mvel expression
	String onFlush() default "";

}
