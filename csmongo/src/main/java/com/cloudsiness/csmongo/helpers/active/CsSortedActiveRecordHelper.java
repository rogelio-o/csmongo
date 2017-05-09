package com.cloudsiness.csmongo.helpers.active;

import java.util.Arrays;

import com.cloudsiness.csmongo.active.CsActiveRecord;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class CsSortedActiveRecordHelper {
	
	private CsActiveRecord<?> activeRecord;
	
	private String attribute;
	
	
	public CsSortedActiveRecordHelper(CsActiveRecord<?> activeRecord, String attribute) {
		this.activeRecord = activeRecord;
		this.attribute = attribute;
	}
	
	public CsSortedActiveRecordHelper(CsActiveRecord<?> activeRecord) {
		this(activeRecord, "position");
	}
	
	public static CsSortedActiveRecordHelper helper(CsActiveRecord<?> activeRecord) {
		return new CsSortedActiveRecordHelper(activeRecord);
	}
	
	public static CsSortedActiveRecordHelper helper(CsActiveRecord<?> activeRecord, String attribute) {
		return new CsSortedActiveRecordHelper(activeRecord, attribute);
	}
	
	
	public void beforeSave(Handler<AsyncResult<Boolean>> handler) {
		beforeSave(new JsonObject(), handler);
	}
	
	public void beforeSave(JsonObject query, Handler<AsyncResult<Boolean>> handler) {
		activeRecord.count(query, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			activeRecord.setAttribute(attribute, res.result());
			
			handler.handle(Future.succeededFuture(true));
		});
	}
	
	public void afterDelete(Handler<AsyncResult<Boolean>> handler) {
		afterDelete(new JsonObject(), handler);
	}
	
	public void afterDelete(JsonObject query, Handler<AsyncResult<Boolean>> handler) {
		JsonObject newQuery = query.mergeIn(new JsonObject()
				.put(attribute, new JsonObject()
						.put("$gt", (Long) activeRecord.getAttributeValue(attribute))));
		JsonObject update = new JsonObject()
				.put("$inc", new JsonObject()
						.put(attribute, -1));
		
		activeRecord.updateAllFree(newQuery, update, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			handler.handle(Future.succeededFuture(true));
		});
	}
	
	
	public void up(Handler<AsyncResult<Void>> handler) {
		up(new JsonObject(), handler);
	}
	
	public void up(JsonObject query, Handler<AsyncResult<Void>> handler) {
		changePosition(query, (Long)activeRecord.getAttributeValue(attribute) + 1, handler);
	}
	
	public void down(Handler<AsyncResult<Void>> handler) {
		down(new JsonObject(), handler);
	}
	
	public void down(JsonObject query, Handler<AsyncResult<Void>> handler) {
		changePosition(query, (Long)activeRecord.getAttributeValue(attribute) - 1, handler);
	}
	
	public void changePosition(long newPosition, Handler<AsyncResult<Void>> handler) {
		changePosition(new JsonObject(), newPosition, handler);
	}
	
	public void changePosition(JsonObject query, long newPosition, Handler<AsyncResult<Void>> handler) {
		Long position = activeRecord.getAttributeValue(attribute);
		activeRecord.setAttribute(attribute, newPosition);
		
		if(newPosition < 0) {
			handler.handle(Future.succeededFuture());
			return;
		}
		
		activeRecord.count(query, countRes -> {
			if(countRes.failed()) {
				handler.handle(Future.failedFuture(countRes.cause()));
				return;
			}
			
			if(newPosition >= countRes.result()) {
				handler.handle(Future.succeededFuture());
				return;
			}
			
			JsonObject otherQuery = query.mergeIn(new JsonObject()
					.put(attribute, newPosition));
			activeRecord.find(otherQuery, findRes -> {
				if(findRes.failed()) {
					handler.handle(Future.failedFuture(findRes.cause()));
					return;
				}
				
				CsActiveRecord<?> other = findRes.result();
				
				Future<Void> f1 = Future.future();
				Future<Void> f2 = Future.future();
				
				if(other != null) {
					other.setAttribute(attribute, position);
					other.saveAttributes(Arrays.asList(attribute), f1.completer());
				} else {
					f1.complete();
				}
				
				activeRecord.saveAttributes(Arrays.asList(attribute), f2.completer());
				
				CompositeFuture.all(f1, f2).setHandler(res -> {
					if(f1.failed() && f2.failed()) {
						handler.handle(Future.failedFuture(res.cause()));
					} else if(f1.failed() && !f2.failed()) {
						handler.handle(Future.failedFuture(f1.cause()));
						if(other != null) {
							other.setAttribute(attribute, newPosition);
							other.saveAttributes(Arrays.asList(attribute), res2 -> {});
						}
					} else if(!f1.failed() && f2.failed()) {
						handler.handle(Future.failedFuture(f2.cause()));
						activeRecord.setAttribute(attribute, position);
						activeRecord.saveAttributes(Arrays.asList(attribute), res2 -> {});
					} else {
						handler.handle(Future.succeededFuture());
					}
				});
			});
		});
	}
	

}
