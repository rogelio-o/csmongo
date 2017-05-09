package com.cloudsiness.csmongo.active.validators.max;

import java.math.BigDecimal;
import java.math.BigInteger;
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
 * The annotated element must be a number whose value must be lower or
 * equal to the specified minimum.
 * <p/>
 * Supported types are:
 * <ul>
 *     <li>{@code BigDecimal}</li>
 *     <li>{@code BigInteger}</li>
 *     <li>{@code byte}, {@code short}, {@code int}, {@code long}, and their respective
 *     wrappers</li>
 * </ul>
 * Note that {@code double} and {@code float} are not supported due to rounding errors
 * (some providers might provide some approximative support).
 * <p/>
 * {@code null} elements are considered valid.
 * 
 * @author	Rogelio R. Orts Cansino
 * @version	0.1
 */
public class MaxValidation implements CsConstraintValidator<Max> {
	
	private String message; 
	private Long value;
	private boolean inclusive;

	@Override
	public void initialize(Max constraintAnnotation) {
		message = constraintAnnotation.message();
		value = constraintAnnotation.value();
		inclusive = constraintAnnotation.inclusive();
	}
	
	public void initialize(String message, Long value, boolean inclusive) {
		this.message = message == null || message.isEmpty() ? Max.MESSAGE : message;
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
			valid = validateValue(b.longValue());
		} else if(o instanceof BigInteger) {
			BigInteger b = (BigInteger) o;
			valid = validateValue(b.longValue());
		} else if(o instanceof byte[]) {
			byte[] b = (byte[]) o;
			valid = validateValue((new BigInteger(b)).longValue());
		} else if(o instanceof Short) {
			Short s = (Short) o;
			valid = validateValue(s.longValue());
		} else if(o instanceof Integer) {
			Integer i = (Integer) o;
			valid = validateValue(i.longValue());
		} else if(o instanceof Long) {
			Long l = (Long) o;
			valid = validateValue(l);
		} else {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only BigDecimal, BigInteger, byte, short, int, long attributes are supported on Max validator.")));
			return;
		}
		
		if(valid) {
			handler.handle(Future.succeededFuture());
		} else {
			handler.handle(Future.succeededFuture(func.apply(message)));
		}
	}
	
	private boolean validateValue(long attrValue) {
		int comparation = value.compareTo(attrValue);
		
		return comparation > 0 || (comparation == 0 && inclusive);
	}

}