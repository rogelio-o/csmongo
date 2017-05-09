package com.cloudsiness.csmongo.active.validators.max;

import java.math.BigDecimal;
import java.util.function.Function;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * /**
 * The annotated element must be a number whose value must be lower or
 * equal to the specified minimum.
 * <p/>
 * Supported types are:
 * <ul>
 *     <li>{@code BigDecimal}</li>
 *     <li>{@code CharSequence}</li>
 *     <li>{@code double}, {@code float} and their respective
 *     wrappers</li>
 * </ul>
 * <p/>
 * {@code null} elements are considered valid.
 * 
 * @author 	Rogelio R. Orts Cansino
 * @version	0.1
 */
public class DecimalMaxValidation implements CsConstraintValidator<DecimalMax> {
	
	private String message; 
	private BigDecimal value;
	private boolean inclusive;

	@Override
	public void initialize(DecimalMax constraintAnnotation) {
		message = constraintAnnotation.message();
		value = new BigDecimal(constraintAnnotation.value());
		inclusive = constraintAnnotation.inclusive();
	}
	
	public void initialize(String message, BigDecimal value, boolean inclusive) {
		this.message = message == null || message.isEmpty() ? DecimalMax.MESSAGE : message;
		this.value = value;
		this.inclusive = inclusive;
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		if(attribute == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object o = model.getAttributeValue(attribute);
		
		isValid(o, message -> new CsValidationError(message, attribute).setValue(value.toString()), handler);
	}
	
	public void isValid(Object o, Handler<AsyncResult<CsValidationError>> handler) {
		isValid(o, message -> new CsValidationError(message), handler);
	}
	
	public void isValid(Object o, Function<String, CsValidationError> func, Handler<AsyncResult<CsValidationError>> handler) {
		if(o == null) { 
			handler.handle(Future.succeededFuture());
			return;
		}
		
		boolean valid = false;
		
		if(o instanceof BigDecimal) {
			BigDecimal b = (BigDecimal) o;
			valid = validateValue(b);
		} else if(o instanceof CharSequence) {
			CharSequence c = (CharSequence) o;
			valid = validateValue(new BigDecimal(c.toString()));
		} else if(o instanceof Double) {
			Double d = (Double) o;
			valid = validateValue(new BigDecimal(d));
		} else if(o instanceof Float) {
			Float f = (Float) o;
			valid = validateValue(new BigDecimal(f));
		} else {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only BigDecimal, CharSequence, double and float attributes are supported on DecimalMax validator.")));
			return;
		}
		
		if(valid) {
			handler.handle(Future.succeededFuture());
		} else {
			handler.handle(Future.succeededFuture(func.apply(message)));
		}
	}
	
	private boolean validateValue(BigDecimal attrValue) {
		int comparation = value.compareTo(attrValue);
		
		return comparation > 0 || (comparation == 0 && inclusive);
	}

}