package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.ironrhino.core.cache.CacheManager;

@Target(METHOD)
@Retention(RUNTIME)
public @interface FlushCache {

	// mvel expression
	String key();

	// mvel expression
	String namespace() default CacheManager.DEFAULT_NAMESPACE;

	// mvel expression
	String onFlush() default "";
	
	String renew() default "";

}
