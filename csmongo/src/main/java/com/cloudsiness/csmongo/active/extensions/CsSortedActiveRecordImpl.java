package com.cloudsiness.csmongo.active.extensions;


import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.helpers.CsFutureHelper;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CsSortedActiveRecordImpl {
	
	private CsActiveRecord<?> model;
	
	private String positionAttribute;
	
	Logger logger = LoggerFactory.getLogger(CsSortedActiveRecordImpl.class);
	
	
	public CsSortedActiveRecordImpl(CsActiveRecord<?> model, String positionAttribute) {
		this.model = model;
		this.positionAttribute = positionAttribute;
	}
	

	public void setInitialPositionOnBeforeSave(Handler<AsyncResult<Boolean>> handler) {
		if(!model.isNewRecord()) {
			handler.handle(Future.succeededFuture(true));
			return;
		}
		
		model.count(res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			model.setAttribute(positionAttribute, res.result());
			
			handler.handle(Future.succeededFuture(true));
		});
	}
	
	public void resetOthersPositionsOnAfterDelete(Handler<AsyncResult<Boolean>> handler) {
		flip(model.getAttributeValue(positionAttribute), null, true, handler);
	}
	
	public void changePosition(int offset, int direction, Handler<AsyncResult<Boolean>> handler) {
		long oldPosition = model.getAttributeValue(positionAttribute);
		long newPosition = direction == CsSortedActiveRecord.DIRECTION_DOWN ? oldPosition + offset : oldPosition - offset;
		
		model.setAttribute(positionAttribute, newPosition);
		
		model.save(res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			if(!res.result()) {
				handler.handle(Future.succeededFuture(false));
				return;
			}
			
			if(direction == CsSortedActiveRecord.DIRECTION_DOWN) {
				flip(oldPosition, newPosition, true, finishHandler(oldPosition, handler));
			} else {
				flip(newPosition, oldPosition, false, finishHandler(oldPosition, handler));
			}
		});
	}
	
	public void changePosition(long newPosition, Handler<AsyncResult<Boolean>> handler) {
		long oldPosition = model.getAttributeValue(positionAttribute);
		boolean down = oldPosition < newPosition;
		long offset = down ? newPosition - oldPosition : oldPosition - newPosition;
		changePosition((int) offset, down ? CsSortedActiveRecord.DIRECTION_DOWN : CsSortedActiveRecord.DIRECTION_UP, handler);
	}
	
	private Handler<AsyncResult<Boolean>> finishHandler(long oldPosition, Handler<AsyncResult<Boolean>> handler) {
		return res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			if(!res.result()) {
				model.setAttribute(positionAttribute, oldPosition);
				model.save(res2 -> {
					if(res2.failed() || !res2.result()) {
						logger.error("Error undoning position.", res2.cause());
					}
					
					handler.handle(res);
				});
				return;
			}
			
			handler.handle(Future.succeededFuture(true));
		};
	}
	
	private void flip(long lowLimit, Long hightLimit, boolean rest, Handler<AsyncResult<Boolean>> handler) {
		JsonObject condition = new JsonObject()
				.put("$gte", lowLimit);
		if(hightLimit != null)
			condition.put("$lte", hightLimit);
		JsonObject query = new JsonObject()
				.put(positionAttribute, condition);
		query.put("_id", new JsonObject()
				.put("$ne", new JsonObject()
						.put("$oid", model._id.toString())
				)
		);
		
		model.findAll(query, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			CsFutureHelper.helper().doInItems(res.result(), (item, f) -> {
				item.setAttribute(positionAttribute, (long) item.getAttributeValue(positionAttribute) + (rest ? -1 : 1));
				item.save(f.completer());
			}, handler);
		});
	}
}
