package org.ironrhino.core.cache;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface FlushCache {

	// mvel expression
	String key();

	// mvel expression
	String namespace() default CacheContext.DEFAULT_CACHE_NAMESPACE;

	// mvel expression
	String onFlush() default "";

}
