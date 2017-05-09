package com.cloudsiness.csmongo.active.helpers;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class TestTypeValidatorValidation implements CsConstraintValidator<TestTypeValidator> {

	private boolean hasError;
	
	@Override
	public void initialize(TestTypeValidator constraintAnnotation) {
		hasError = constraintAnnotation.hasError();
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		handler.handle(Future.succeededFuture(hasError ? new CsValidationError("error", attribute) : null));
	}

}
