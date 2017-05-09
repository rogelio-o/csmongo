package com.cloudsiness.csmongo.active.validators.required;

import java.util.function.Function;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class RequiredValidation implements CsConstraintValidator<Required> {
	
	private String message; 

	@Override
	public void initialize(Required constraintAnnotation) {
		message = constraintAnnotation.message();
	}
	
	public void initialize(String message) {
		this.message = message == null || message.isEmpty() ? Required.MESSAGE : message;
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
		if(o == null || (o instanceof String && ((String) o).isEmpty())) {
			handler.handle(Future.succeededFuture(func.apply(message)));
		} else {
			handler.handle(Future.succeededFuture());
		}
	}

}
