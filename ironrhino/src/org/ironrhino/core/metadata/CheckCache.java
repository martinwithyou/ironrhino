package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.ironrhino.core.cache.CacheManager;

@Target(METHOD)
@Retention(RUNTIME)
public @interface CheckCache {
	// mvel expression
	String key();

	// mvel expression
	String namespace() default CacheManager.DEFAULT_NAMESPACE;

	// mvel expression
	String when() default "";

	// mvel expression
	String timeToLive() default CacheManager.DEFAULT_TIME_TO_LIVE;

	// mvel expression
	String timeToIdle() default CacheManager.DEFAULT_TIME_TO_IDLE;
	
	boolean eternal() default false;

	// mvel expression
	String onHit() default "";

	// mvel expression
	String onMiss() default "";

	// mvel expression
	String onPut() default "";

}
