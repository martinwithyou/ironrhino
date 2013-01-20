package org.ironrhino.core.aop;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD })
@Retention(RUNTIME)
public @interface ConcurrencyControl {

	/**
	 * 
	 * this attribute support expression
	 * 
	 * @return
	 */
	String permits();

	/**
	 * 
	 * this attribute support expression
	 * 
	 * @return
	 */
	String fair() default "false";

	boolean block() default false;

	/**
	 * in MILLISECONDS
	 * 
	 * @return
	 */
	long timeout() default 0;

}
