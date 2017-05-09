package com.cloudsiness.csmongo.active.validators.unique;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={UniqueValidation.class})
public @interface Unique {
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default "{com.cloudiness.csmongo.Unique.message}";
	boolean canBeNull() default false;
	boolean nullIsAValue() default false;
}
