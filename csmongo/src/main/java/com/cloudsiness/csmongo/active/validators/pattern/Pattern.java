package com.cloudsiness.csmongo.active.validators.pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={PatternValidation.class})
public @interface Pattern {
	public static final String MESSAGE = "{com.cloudiness.csmongo.Pattern.message}";
	
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default MESSAGE;
	String regexp();
	int[] flags() default {};	
}
