package com.cloudsiness.csmongo.active.validators.size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={SizeValidation.class})
public @interface Size {
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default "{com.cloudiness.csmongo.Size.message}";
	int min() default 0;
	int max() default Integer.MAX_VALUE;
	boolean allowEmpty() default false;
}
