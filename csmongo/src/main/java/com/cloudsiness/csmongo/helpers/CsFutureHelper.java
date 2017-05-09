package com.cloudsiness.csmongo.helpers;

import java.util.ArrayList;
import java.util.List;

import com.cloudsiness.csmongo.helpers.functions.Consumer2;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class CsFutureHelper {

	public static CsFutureHelper helper() {
		return new CsFutureHelper();
	}
	
	@SuppressWarnings("rawtypes")
	public <T> void doInItems(Iterable<T> iterable, Consumer2<T, Future<Boolean>> function, Handler<AsyncResult<Boolean>> handler) {
		List<Future> futures = new ArrayList<Future>();
		
		if(iterable != null) {
			for(T item : iterable) {
				Future<Boolean> f = Future.future();
				futures.add(f);
				
				function.accept(item, f);
			}
		}
		
		if(futures.isEmpty()) {
			handler.handle(Future.succeededFuture(true));
			return;
		}
		
		CompositeFuture.all(futures).setHandler(CsFutureHelper.helper().fromBooleanToComposite(futures, handler));
	}
	
	@SuppressWarnings("rawtypes")
	public <T> void doInItemsVoid(Iterable<T> iterable, Consumer2<T, Future<Void>> function, Handler<AsyncResult<Void>> handler) {
		List<Future> futures = new ArrayList<Future>();
		
		if(iterable != null) {
			for(T item : iterable) {
				Future<Void> f = Future.future();
				futures.add(f);
				
				function.accept(item, f);
			}
		}
		
		if(futures.isEmpty()) {
			handler.handle(Future.succeededFuture());
			return;
		}
		
		CompositeFuture.all(futures).setHandler(CsFutureHelper.helper().fromVoidToComposite(handler));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Handler<AsyncResult<CompositeFuture>> fromBooleanToComposite(List<Future> futures, Handler<AsyncResult<Boolean>> handler) {
		return res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			boolean result = true;
			for(Future<Boolean> f : futures) {
				if(!f.result()) {
					result = false;
					break;
				}
			}
			
			handler.handle(Future.succeededFuture(result));
		};
	}
	
	public Handler<AsyncResult<CompositeFuture>> fromVoidToComposite(Handler<AsyncResult<Void>> handler) {
		return res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			handler.handle(Future.succeededFuture());
		};
	}
	
}
