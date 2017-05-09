package com.cloudsiness.csmongo.active.validators.past;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class PastValidation implements CsConstraintValidator<Past> {

	private String message; 

	@Override
	public void initialize(Past constraintAnnotation) {
		message = constraintAnnotation.message();
	}
	
	public void initialize(String message) {
		this.message = message == null || message.isEmpty() ? Past.MESSAGE : message;
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
		if(o == null) {
			handler.handle(Future.succeededFuture());
			return;
		} else if(o instanceof Date) {
			Date d = (Date) o;
			if(d.before(new Date())) {
				handler.handle(Future.succeededFuture());
				return;
			}
		} else if(o instanceof Calendar) {
			Calendar c = (Calendar) o;
			if(c.getTime().before(new Date())) {
				handler.handle(Future.succeededFuture());
				return;
			}
		} else {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only Date and Calendar attributes are supported on Past validator.")));
			return;
		}
		
		handler.handle(Future.succeededFuture(func.apply(message)));
	}

}
