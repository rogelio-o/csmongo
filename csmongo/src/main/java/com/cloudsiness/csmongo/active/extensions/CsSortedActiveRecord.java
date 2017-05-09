package com.cloudsiness.csmongo.active.extensions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface CsSortedActiveRecord {
	
	public static final int DIRECTION_UP = 0;
	public static final int DIRECTION_DOWN = 1;
	

	public void setInitialPositionOnBeforeSave(Handler<AsyncResult<Boolean>> handler);
	
	public void resetOthersPositionsOnAfterDelete(Handler<AsyncResult<Boolean>> handler);
	
	public void changePosition(int offset, int direction, Handler<AsyncResult<Boolean>> handler);
	
	public void changePosition(long newPosition, Handler<AsyncResult<Boolean>> handler);
	
}
