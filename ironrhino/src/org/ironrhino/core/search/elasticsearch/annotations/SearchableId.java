package org.ironrhino.core.search.elasticsearch.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RUNTIME)
public @interface SearchableId {

	boolean override() default true;

	String idConverter() default "";

	String accessor() default "";

	String name() default "";

	float boost() default 1.0f;

	Store store() default Store.NA;

	Index index() default Index.NA;

	String analyzer() default "";

	ExcludeFromAll excludeFromAll() default ExcludeFromAll.NO;

	String converter() default "";

	String format() default "";

}
