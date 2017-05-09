package com.cloudsiness.csmongo.active.validators;

import java.lang.annotation.Annotation;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class CsValidator {
	
	/**
	 * The class for to do the validation.
	 */
	private Class<? extends CsConstraintValidator<Annotation>> constraintValidatorClass;
	
	/**
	 * The annotation that marked the field with the constraint validator. 
	 * Allows initialize the constraint validator class.
	 */
	private Annotation annotation;
	
	/**
	 * The scenario in which the constraint is being validated.
	 */
	private String scenario;
	
	/**
	 * Default constructor. Private because this class only can be initialized with the static method #create.
	 */
	@SuppressWarnings("unchecked")
	private CsValidator(Class<? extends CsConstraintValidator<? extends Annotation>> constraintValidatorClass, Annotation annotation, String scenario) {
		this.constraintValidatorClass = (Class<? extends CsConstraintValidator<Annotation>>) constraintValidatorClass;
		this.annotation = annotation;
		this.scenario = scenario;
	}
	
	/**
	 * Way of to instantiate a CsValidator.
	 */
	public static CsValidator create(Class<? extends CsConstraintValidator<? extends Annotation>> constraintValidatorClass, Annotation annotation, String scenario) {
		return new CsValidator(constraintValidatorClass, annotation, scenario);
	}
	
	/**
	 * Validates the object listed in parameters with the constraint validator class.
	 * 
	 * @param o	the object to validate.
	 * @return	the error message or null if the object has no error.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws CsValidatorNotValidForAttributeType 
	 * @throws CsConstraintNotValidInModel 
	 */
	public void validate(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) throws InstantiationException, IllegalAccessException {
		CsConstraintValidator<Annotation> constraintValidator = constraintValidatorClass.newInstance();
		
		constraintValidator.initialize(annotation);
		constraintValidator.isValid(model, attribute, handler);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((constraintValidatorClass == null) ? 0 : constraintValidatorClass.hashCode());
		result = prime * result + ((scenario == null) ? 0 : scenario.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CsValidator other = (CsValidator) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		if (constraintValidatorClass == null) {
			if (other.constraintValidatorClass != null)
				return false;
		} else if (!constraintValidatorClass.equals(other.constraintValidatorClass))
			return false;
		if (scenario == null) {
			if (other.scenario != null)
				return false;
		} else if (!scenario.equals(other.scenario))
			return false;
		return true;
	}
	
	
}
