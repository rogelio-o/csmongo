package com.cloudsiness.csmongo.active.validators.size;

import java.util.Collection;
import java.util.Map;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class SizeValidation implements CsConstraintValidator<Size> {
	
	private String message; 
	private int min;
	private int max;
	private boolean allowEmpty;

	@Override
	public void initialize(Size constraintAnnotation) {
		message = constraintAnnotation.message();
		min = constraintAnnotation.min();
		max = constraintAnnotation.max();
		allowEmpty = constraintAnnotation.allowEmpty();
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		if(attribute == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object o = model.getAttributeValue(attribute);
		if(o == null) {
			handler.handle(Future.succeededFuture());
			return;
		} else if(o instanceof CharSequence) {
			CharSequence c = (CharSequence) o;
			if(validateValue(c.length())) {
				handler.handle(Future.succeededFuture());
				return;
			}
			
		} else if(o instanceof Collection) {
			Collection<?> c = (Collection<?>) o;
			if(validateValue(c.size())) {
				handler.handle(Future.succeededFuture());
				return;
			}
			
		} else if(o instanceof Map) {
			Map<?, ?> m = (Map<?, ?>) o;
			if(validateValue(m.size())) {
				handler.handle(Future.succeededFuture());
				return;
			}
			
		} else if(o instanceof Object[]) {
			Object[] os = (Object[]) o;
			if(validateValue(os.length)) {
				handler.handle(Future.succeededFuture());
				return;
			}
		} else {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only CharSequence, Collecion, Map and array attributes are supported on Size validator.")));
			return;
		}
		
		handler.handle(Future.succeededFuture(new CsValidationError(message, attribute).setValue(String.valueOf(min)).setValue2(String.valueOf(max))));
	}
	
	private boolean validateValue(int v) {
		if(allowEmpty && v == 0)
			return true;
		
		return v >= min && v <= max;
	}

}
