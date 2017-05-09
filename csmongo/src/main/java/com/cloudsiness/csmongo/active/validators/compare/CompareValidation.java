package com.cloudsiness.csmongo.active.validators.compare;

import java.util.function.Function;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class CompareValidation implements CsConstraintValidator<Compare> {
	
	private String message;
	private String attribute;

	@Override
	public void initialize(Compare constraintAnnotation) {
		message = constraintAnnotation.message();
		attribute = constraintAnnotation.attribute();
	}
	
	public void initialize(String message) {
		this.message = message == null || message.isEmpty() ? Compare.MESSAGE : message;
	}

	@Override
	public void isValid(CsActiveForm<?> model, String field, Handler<AsyncResult<CsValidationError>> handler) {
		if(field == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object attr1 = model.getAttributeValue(field);
		Object attr2 = model.getAttributeValue(attribute);
		
		isValid(attr1, attr2, message -> new CsValidationError(message, field, attribute), handler);
	}
	
	public void isValid(Object attr1, Object attr2, Handler<AsyncResult<CsValidationError>> handler) {
		isValid(attr1, attr2, message -> new CsValidationError(message), handler);
	}
	
	public void isValid(Object attr1, Object attr2, Function<String, CsValidationError> func, Handler<AsyncResult<CsValidationError>> handler) {
		if(attr1 == null && attr2 == null) {
			handler.handle(Future.succeededFuture());
		} else if(attr1 != null && attr2 != null) {
			if(attr1.equals(attr2)) {
				handler.handle(Future.succeededFuture());
			} else {
				handler.handle(Future.succeededFuture(func.apply(message)));
			}
		} else {
			handler.handle(Future.succeededFuture(func.apply(message)));
		}
	}

}
