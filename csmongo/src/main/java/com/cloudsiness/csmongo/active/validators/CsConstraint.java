package com.cloudsiness.csmongo.active.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CsConstraint {
	
	/**
	 * {@link CsConstraintValidator} classes must reference distinct classes for to use
	 * to validate when the annotation annotated with CsConstraint is used in a attribute.
	 */
	Class<? extends CsConstraintValidator<?>>[] validatedBy();
	
}
