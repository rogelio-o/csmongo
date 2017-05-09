package com.cloudsiness.csmongo.active.validators;

import java.lang.annotation.Annotation;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * Defines the logic to validate a given constraint {@code A}
 * for a given object type {@code T}.
 * 
 * @author 		Rogelio R. Orts Cansino
 * @version 	0.1
 */
public interface CsConstraintValidator<A extends Annotation> {
	
	/**
	 * Initializes the validator in preparation for
	 * {@link #isValid(Object, ConstraintValidatorContext)} calls.
	 * The constraint annotation for a given constraint declaration
	 * is passed.
	 * <p/>
	 * This method is guaranteed to be called before any use of this instance for
	 * validation.
	 *
	 * @param constraintAnnotation annotation instance for a given constraint declaration
	 */
	public void initialize(A constraintAnnotation);
	
	/**
	 * Implements the validation logic.
	 * The state of {@code value} must not be altered.
	 * 
	 * @param value object to validate
	 * @return If it is valid, return null. Otherwise, it returns the error message.
	 * @throws CsValidatorNotValidForAttributeType
	 * @throws CsConstraintNotValidInModel 
	 */
	public void isValid(CsActiveForm<?> model, String field, Handler<AsyncResult<CsValidationError>> handler);
	
}
