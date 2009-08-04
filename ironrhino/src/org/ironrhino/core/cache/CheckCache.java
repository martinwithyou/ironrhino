package org.ironrhino.core.cache;

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

	String name() default CacheContext.DEFAULT_CACHE_NAME;

	String when() default "";

	int timeToLive() default CacheContext.DEFAULT_TIME_TO_LIVE;

	int timeToIdle() default CacheContext.DEFAULT_TIME_TO_IDLE;
}
