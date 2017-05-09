package com.cloudsiness.csmongo.active.validators.max;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraint;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CsConstraint(validatedBy={DecimalMaxValidation.class})
public @interface DecimalMax {
	public static final String MESSAGE = "{com.cloudiness.csmongo.DecimalMax.message}";
	
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
	String message() default MESSAGE;
	
	/**
	 * The {@code String} representation of the max value according to the
	 * {@code BigDecimal} string representation.
	 * 
	 * @return value the element must be lower or equal to
	 */
	String value();
	
	/**
	 * Specifies whether the specified maximum is inclusive or exclusive.
	 * By default, it is inclusive.
	 *
	 * @return {@code true} if the value must be lower or equal to the specified minimum,
	 *         {@code false} if the value must be lower
	 *
	 * @since 1.1
	 */
	boolean inclusive() default true;
}
