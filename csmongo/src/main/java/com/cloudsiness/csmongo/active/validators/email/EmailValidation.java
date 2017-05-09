package com.cloudsiness.csmongo.active.validators.email;

import java.util.function.Function;
import java.util.regex.Pattern;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class EmailValidation implements CsConstraintValidator<Email> {
	
	private String message;
	private Pattern pattern;

	@Override
	public void initialize(Email constraintAnnotation) {
		message = constraintAnnotation.message();
		pattern = Pattern.compile(constraintAnnotation.pattern());
	}
	
	public void initialize(String message) {
		this.message = message == null || message.isEmpty() ? Email.MESSAGE : message;
		pattern = Pattern.compile(Email.PATTERN);
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		if(attribute == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object o = model.getAttributeValue(attribute);
		
		isValid(o, message -> new CsValidationError(message, attribute), handler);
	}
	
	public void isValid(Object o, Handler<AsyncResult<CsValidationError>> handler) {
		isValid(o, message -> new CsValidationError(message), handler);
	}
	
	public void isValid(Object o, Function<String, CsValidationError> func, Handler<AsyncResult<CsValidationError>> handler) {
		if(o != null && !(o instanceof CharSequence)) {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only String attributes are supported on this validator.")));
			return;
		}
		
		String email = (String) o;
		if(!validateValue(email)) {
			handler.handle(Future.succeededFuture(func.apply(message)));
		} else {
			handler.handle(Future.succeededFuture());
		}
	}
	
	private boolean validateValue(String email) {
		if(email == null || email.isEmpty())
			return true;
		
		return email != null && email.length() <= 254 && pattern.matcher(email).matches();
	}

}
