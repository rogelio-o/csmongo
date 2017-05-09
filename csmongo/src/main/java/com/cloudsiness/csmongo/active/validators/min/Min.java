package com.cloudsiness.csmongo.active.validators.min;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={MinValidation.class})
public @interface Min {
	public static final String MESSAGE = "{com.cloudiness.csmongo.Min.message}";
	
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default MESSAGE;
	
	/**
	 * @return value the element must be higher or equal to
	 */
	long value();
	
	/**
	 * Specifies whether the specified minimum is inclusive or exclusive.
	 * By default, it is inclusive.
	 *
	 * @return {@code true} if the value must be higher or equal to the specified minimum,
	 *         {@code false} if the value must be higher
	 *
	 * @since 1.1
	 */
	boolean inclusive() default true;
}
