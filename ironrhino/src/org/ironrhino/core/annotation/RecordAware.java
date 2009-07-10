package org.ironrhino.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use for record model's CRUD operations
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.aspect.Recording
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
public @interface RecordAware {
}
