package com.octo.tools.crud.rest.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Target({FIELD})
@Retention(RUNTIME)
public @interface RestResourceMapper {
	
	static final String RESOURCE_ID_PLACEHOLDER = "#id";
	
    boolean external() default false;
    String context();
    String path() default RESOURCE_ID_PLACEHOLDER;
    String resolveToProperty() default "";
    String auditContext() default "";
    String lastRevisionPath() default "";
}
