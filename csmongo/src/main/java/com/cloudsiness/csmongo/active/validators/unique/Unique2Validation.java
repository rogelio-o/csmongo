package com.cloudsiness.csmongo.active.validators.unique;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class Unique2Validation implements CsConstraintValidator<Unique2> {

	private String attribute2;
	
	private String message;
	
	private boolean canBeNull;
	
	private boolean nullIsAValue;
	
	@Override
	public void initialize(Unique2 constraintAnnotation) {
		attribute2 = constraintAnnotation.attribute2();
		message = constraintAnnotation.message();
		canBeNull = constraintAnnotation.canBeNull();
		nullIsAValue = constraintAnnotation.nullIsAValue();
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		if(attribute == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object o = model.getAttributeValue(attribute);
		Object o2 = model.getAttributeValue(attribute2);
		
		if(o == null || o2 == null) {
			if(!canBeNull) {
				handler.handle(Future.succeededFuture(new CsValidationError(message, attribute, attribute2)));
				return;
			} else if(!nullIsAValue) {
				handler.handle(Future.succeededFuture());
				return;
			}
		}
		
		try {
			CsActiveRecord<?> activeModel = (CsActiveRecord<?>)model;
			JsonObject query =  new JsonObject();
			UniqueValidation.addAttributeToQuery(query, attribute, o);
			UniqueValidation.addAttributeToQuery(query, attribute2, o2);
			if(activeModel._id != null)
				query.put("_id", new JsonObject().put("$ne", new JsonObject().put("$oid", activeModel._id.toString())));
			activeModel.exists(query, res -> {
				if(res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				} else {
					handler.handle(Future.succeededFuture(res.result() ? new CsValidationError(message, attribute, attribute2) : null));
				}
			});
		} catch(Exception e){
			handler.handle(Future.failedFuture(e));
		}
	}
	
	
	
}
