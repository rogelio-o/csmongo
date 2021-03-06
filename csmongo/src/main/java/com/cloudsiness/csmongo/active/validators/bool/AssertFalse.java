package com.cloudsiness.csmongo.active.validators.bool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={AssertFalseValidation.class})
public @interface AssertFalse {
	public static final String MESSAGE = "{com.cloudiness.csmongo.AssertFalse.message}";
	
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default MESSAGE;
}
