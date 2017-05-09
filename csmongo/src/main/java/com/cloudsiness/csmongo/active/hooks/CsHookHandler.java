package com.cloudsiness.csmongo.active.hooks;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface CsHookHandler<T> {
	
	public void handle(T model, Handler<AsyncResult<Boolean>> result);
	
}
