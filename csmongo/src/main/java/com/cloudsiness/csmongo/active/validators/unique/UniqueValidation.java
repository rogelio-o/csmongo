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

public class UniqueValidation implements CsConstraintValidator<Unique> {
	
	private String message;
	
	private boolean canBeNull;
	
	private boolean nullIsAValue;
	
	@Override
	public void initialize(Unique constraintAnnotation) {
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
		
		if(o == null) {
			if(!canBeNull) {
				handler.handle(Future.succeededFuture(new CsValidationError(message, attribute)));
				return;
			} else if(!nullIsAValue) {
				handler.handle(Future.succeededFuture());
				return;
			}
		}
		
		try {
			CsActiveRecord<?> activeModel = (CsActiveRecord<?>)model;
			JsonObject query = new JsonObject();
			addAttributeToQuery(query, attribute, o);
			if(activeModel._id != null)
				query.put("_id", new JsonObject().put("$ne", new JsonObject().put("$oid", activeModel._id.toString())));
			activeModel.exists(query, res -> {
				if(res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				} else {
					handler.handle(Future.succeededFuture(res.result() ? new CsValidationError(message, attribute) : null));
				}
			});
		} catch(Exception e){
			handler.handle(Future.failedFuture(e));
		}
	}
	
	
	public static void addAttributeToQuery(JsonObject query, String attribute, Object o) {
		if(o == null) {
			query.put(attribute, new JsonObject().put("$exists", false));
		} else {
			query.put(attribute, o);
		}
	}
	
}
