package org.ironrhino.core.search.elasticsearch.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableProperty {

	Class<?> type() default Object.class;

	boolean override() default true;

	String propertyConverter() default "";

	String accessor() default "";

	String name() default "";

	float boost() default 1.0f;

	Store store() default Store.NA;

	Index index() default Index.NA;

	String analyzer() default "";

	ExcludeFromAll excludeFromAll() default ExcludeFromAll.NO;

	String converter() default "";

	String format() default "";

	String nullValue() default "";

}
