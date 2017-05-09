package com.cloudsiness.csmongo.active.validators.digits;

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
 * The annotated element must be a number within accepted range
 * Supported types are:
 * <ul>
 *     <li>{@code BigDecimal}</li>
 *     <li>{@code BigInteger}</li>
 *     <li>{@code CharSequence}</li>
 *     <li>{@code byte}, {@code short}, {@code int}, {@code long}, {@ double}, {@float}, and their respective
 *     wrapper types</li>
 * </ul>
 * <p/>
 * {@code null} elements are considered valid.
 *
 * @author 	Rogelio R. Orts Cansino
 * @version 0.1
 */
public class DigitsValidation implements CsConstraintValidator<Digits> {
	
	private String message; 
	private int integer;
	private int fraction;

	@Override
	public void initialize(Digits constraintAnnotation) {
		message = constraintAnnotation.message();
		integer = constraintAnnotation.integer();
		fraction = constraintAnnotation.fraction();
	}
	
	public void initialize(String message, int integer, int fraction) {
		this.message = message == null || message.isEmpty() ? Digits.MESSAGE : message;
		this.integer = integer;
		this.fraction = fraction;
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		if(attribute == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object o = model.getAttributeValue(attribute);
		
		isValid(o, message -> new CsValidationError(message, attribute).setValue(String.valueOf(integer)).setValue2(String.valueOf(fraction)), handler);
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
			valid = validateValue(b.toString());
		} else if(o instanceof BigInteger) {
			BigInteger b = (BigInteger) o;
			valid = validateValue(b.toString());
		} else if(o instanceof CharSequence) {
			CharSequence c = (CharSequence) o;
			valid = validateValue(c.toString());
		} else if(o instanceof byte[]) {
			byte[] b = (byte[]) o;
			valid = validateValue((new BigInteger(b)).toString());
		} else if(o instanceof Short) {
			Short s = (Short) o;
			valid = validateValue(s.toString());
		} else if(o instanceof Integer) {
			Integer i = (Integer) o;
			valid = validateValue(i.toString());
		} else if(o instanceof Long) {
			Long l = (Long) o;
			valid = validateValue(l.toString());
		} else if(o instanceof Double) {
			Double d = (Double) o;
			valid = validateValue(d.toString());
		} else if(o instanceof Float) {
			Float f = (Float) o;
			valid = validateValue(f.toString());
		} else {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only BigDecimal, BigInteger, CharSequence, byte, short, int, long, double and float attributes are supported on Digits validator.")));
			return;
		}
		
		if(valid) {
			handler.handle(Future.succeededFuture());
		} else {
			handler.handle(Future.succeededFuture(func.apply(message)));
		}
	}
	
	private boolean validateValue(String s) {
		int integerPlaces = s.indexOf(".");
		int decimalPlaces;
		if(integerPlaces == -1) {
			integerPlaces = s.length();
			decimalPlaces = 0;
		} else {
			decimalPlaces = s.length() - integerPlaces - 1;
		}
		
		return integerPlaces <= integer && decimalPlaces <= fraction;
	}

}