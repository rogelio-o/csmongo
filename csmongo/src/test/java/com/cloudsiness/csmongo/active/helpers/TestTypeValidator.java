package com.cloudsiness.csmongo.active.helpers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CsConstraint(validatedBy={TestTypeValidatorValidation.class})
public @interface TestTypeValidator {
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	boolean hasError() default false;
}
