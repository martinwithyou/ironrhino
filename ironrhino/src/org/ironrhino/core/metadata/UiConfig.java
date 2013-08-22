package org.ironrhino.core.metadata;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface UiConfig {

	public static final String DEFAULT_TYPE = "input";

	public static final String DEFAULT_INPUT_TYPE = "text";

	public static final String DEFAULT_LIST_KEY = "id";

	public static final String DEFAULT_LIST_VALUE = "name";

	String alias() default "";

	String id() default "";

	String type() default DEFAULT_TYPE; // input,textarea,enum,checkbox,listpick,dictionary,schema...

	String inputType() default DEFAULT_INPUT_TYPE; // text,password,email,number...

	int maxlength() default 0;

	String regex() default "";

	String cssClass() default "";

	int displayOrder() default Integer.MAX_VALUE;

	boolean required() default false;

	boolean unique() default false;

	boolean readonly() default false;

	String readonlyExpression() default "";

	boolean hidden() default false;

	HiddenConfig hiddenInList() default @HiddenConfig;

	HiddenConfig hiddenInInput() default @HiddenConfig;

	HiddenConfig hiddenInView() default @HiddenConfig;

	boolean searchable() default false;

	String template() default "";

	String listTemplate() default "";

	String viewTemplate() default "";

	String width() default "";

	String dynamicAttributes() default ""; // json map

	String cellDynamicAttributes() default ""; // json map

	boolean excludeIfNotEdited() default false;

	String listKey() default DEFAULT_LIST_KEY;

	String listValue() default DEFAULT_LIST_VALUE;

	String cellEdit() default "";

	String pickUrl() default "";// for listpick

	String templateName() default ""; // for dictionary,schema

	boolean excludedFromLike() default false;

	boolean excludedFromCriterion() default false;

}