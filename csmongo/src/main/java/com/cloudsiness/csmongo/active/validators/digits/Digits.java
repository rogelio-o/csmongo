package com.cloudsiness.csmongo.active.validators.digits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={DigitsValidation.class})
public @interface Digits {
	public static final String MESSAGE = "{com.cloudiness.csmongo.Digits.message}";
	
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default MESSAGE;
	
	/**
	 * @return maximum number of integral digits accepted for this number
	 */
	int integer();
	
	/**
	 * @return maximum number of fractional digits accepted for this number
	 */
	int fraction();
}
