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

	String type() default DEFAULT_TYPE; // input,textarea,select,checkbox,listpick,dictionary,schema...

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

	boolean hiddenInList() default false;

	boolean hiddenInInput() default false;

	boolean searchable() default false;

	String template() default "";

	String width() default "";

	String dynamicAttributes() default ""; // json map

	boolean excludeIfNotEdited() default false;

	String listKey() default DEFAULT_LIST_KEY;

	String listValue() default DEFAULT_LIST_VALUE;

	String cellEdit() default "";

	String pickUrl() default "";// for listpick

	String templateName() default ""; // for dictionary,schema

}