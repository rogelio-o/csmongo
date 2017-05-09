package com.cloudsiness.csmongo.helpers.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudsiness.csmongo.factory.CsMongoFactory;
import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class CsMongoTestHelper {
	
	public static final int FIND_ERROR = 0;
	public static final int FIND_NOT_FOUND = 1;
	public static final int FIND_FOUND = 2;
	
	public static final int SAVE_ERROR = 0;
	public static final int SAVE_OK = 1;
	
	public static final int DELETE_ERROR = 0;
	public static final int DELETE_OK = 1;
	
	public static final String DEFAULT_ID = "56928c11dd59030532407aed";
	
	private Map<String, Integer> findOneRes;
	private Map<String, Integer> findRes;
	private Map<String, Integer> countRes;
	private Map<String, Integer> saveRes;
	private Map<String, Integer> updateRes;
	private Map<String, Integer> removeOneRes;
	private Map<String, Integer> removeRes;
	
	private Map<String, JsonObject> findOneRet;
	private Map<String, List<JsonObject>> findRet;
	private Map<String, Long> countRet;
	private Map<String, JsonObject> saveRet;
	private Map<String, JsonObject> updateRet;
	private Map<String, Boolean> removeOneRet;
	private Map<String, Boolean> removeRet;
	
	
	public CsMongoTestHelper() {
		findOneRes = new HashMap<String, Integer>();
		findRes = new HashMap<String, Integer>();
		countRes = new HashMap<String, Integer>();
		saveRes = new HashMap<String, Integer>();
		updateRes = new HashMap<String, Integer>();
		removeOneRes = new HashMap<String, Integer>();
		removeRes = new HashMap<String, Integer>();
		
		findOneRet = new HashMap<String, JsonObject>();
		findRet = new HashMap<String, List<JsonObject>>();
		countRet = new HashMap<String, Long>();
		saveRet = new HashMap<String, JsonObject>();
		updateRet = new HashMap<String, JsonObject>();
		removeOneRet = new HashMap<String, Boolean>();
		removeRet = new HashMap<String, Boolean>();
		
	}
	
	
	public void setFindOneRes(String collection, Integer res) {
		findOneRes.put(collection, res);
	}
	
	public void setFindRes(String collection, Integer res) {
		findRes.put(collection, res);
	}
	
	public void setCountRes(String collection, Integer res) {
		countRes.put(collection, res);
	}
	
	public void setSaveRes(String collection, Integer res) {
		saveRes.put(collection, res);
	}
	
	public void setUpdateRes(String collection, Integer res) {
		updateRes.put(collection, res);
	}
	
	public void setRemoveOneRes(String collection, Integer res) {
		removeOneRes.put(collection, res);
	}
	
	public void setRemoveRes(String collection, Integer res) {
		removeRes.put(collection, res);
	}
	
	
	public void setFindOneRet(String collection, JsonObject ret) {
		findOneRet.put(collection, ret);
	}
	
	public void setFindRet(String collection, List<JsonObject> ret) {
		findRet.put(collection, ret);
	}
	
	public void setCountRet(String collection, Long ret) {
		countRet.put(collection, ret);
	}
	
	public JsonObject getSaveRet(String collection) {
		return saveRet.get(collection);
	}
	
	public JsonObject getUpdateRet(String collection) {
		return updateRet.get(collection);
	}
	
	public Boolean getRemoveOneRet(String collection) {
		return removeOneRet.get(collection);
	}
	
	public Boolean getRemoveRet(String collection) {
		return removeRet.get(collection);
	}

	
	@SuppressWarnings("unchecked")
	public void doMock() throws CsClientNotInitializedException {
		mockStatic(CsMongoFactory.class);
		MongoClient client = mock(MongoClient.class);
		
		when(client.findOne(any(), any(), any(), any())).thenAnswer(invoke -> {
			doMockFindOne(invoke.getArgumentAt(0, String.class), invoke.getArgumentAt(3, Handler.class));
			
			return client;
		});
		
		when(client.find(any(), any(), any())).thenAnswer(invoke -> {
			doMockFind(invoke.getArgumentAt(0, String.class), invoke.getArgumentAt(2, Handler.class));
			
			return client;
		});
		
		when(client.count(any(), any(), any())).thenAnswer(invoke -> {
			doMockCount(invoke.getArgumentAt(0, String.class), invoke.getArgumentAt(2, Handler.class));
			
			return client;
		});
		
		when(client.save(any(), any(), any())).thenAnswer(invoke -> {
			doMockSave(
					invoke.getArgumentAt(0, String.class), 
					invoke.getArgumentAt(1, JsonObject.class), 
					invoke.getArgumentAt(2, Handler.class));
			
			return client;
		});
		
		when(client.updateCollection(any(), any(), any(), any())).thenAnswer(invoke -> {
			doMockUpdate(
					invoke.getArgumentAt(0, String.class), 
					invoke.getArgumentAt(2, JsonObject.class), 
					invoke.getArgumentAt(3, Handler.class));
			
			return client;
		});
		
		when(client.removeDocument(any(), any(), any())).thenAnswer(invoke -> {
			doMockRemoveOne(
					invoke.getArgumentAt(0, String.class), 
					invoke.getArgumentAt(2, Handler.class));
			
			return client;
		});
		
		when(client.removeDocuments(any(), any(), any())).thenAnswer(invoke -> {
			doMockRemove(
					invoke.getArgumentAt(0, String.class), 
					invoke.getArgumentAt(2, Handler.class));
			
			return client;
		});
		
		when(CsMongoFactory.mongo()).thenReturn(client);
		when(CsMongoFactory.mongo(any())).thenReturn(client);
	}
	
	private void doMockFindOne(String collection, Handler<AsyncResult<JsonObject>> handler) {
		Integer res = findOneRes.get(collection);
		
		if(res == null || res == FIND_FOUND) {
			JsonObject result = new JsonObject()
					.put("_id", new JsonObject().put("$oid", DEFAULT_ID));
			
			JsonObject ret = findOneRet.get(collection);
			if(ret != null)
				result = result.mergeIn(ret);
					
			handler.handle(Future.succeededFuture(result));
		} else if(res == FIND_NOT_FOUND) {
			handler.handle(Future.succeededFuture());
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
	private void doMockFind(String collection, Handler<AsyncResult<List<JsonObject>>> handler) {
		Integer res = findRes.get(collection);
		
		if(res == null || res != FIND_ERROR) {
			List<JsonObject> result = new ArrayList<JsonObject>();
			
			if(res != null && res == FIND_FOUND) {
				List<JsonObject> ret = findRet.get(collection);
				if(ret != null)
					result = ret;
			}
					
			handler.handle(Future.succeededFuture(result));
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
	private void doMockCount(String collection, Handler<AsyncResult<Long>> handler) {
		Integer res = countRes.get(collection);
		
		if(res == null || res != FIND_ERROR) {
			Long result = 0L;
			
			if(res != null && res == FIND_FOUND) {
				Long ret = countRet.get(collection);
				if(ret != null)
					result = ret;
			}
			
			handler.handle(Future.succeededFuture(result));
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
	private void doMockSave(String collection, JsonObject result, Handler<AsyncResult<String>> handler) {
		Integer res = saveRes.get(collection);
		
		if(res == null || res == SAVE_OK) {
			String id = DEFAULT_ID;
			if(result.getString("_id") != null)
				id = result.getString("_id");
			
			saveRet.put(collection, result);
			
			handler.handle(Future.succeededFuture(id));
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
	private void doMockUpdate(String collection, JsonObject result, Handler<AsyncResult<Void>> handler) {
		Integer res = updateRes.get(collection);
		
		if(res == null || res == SAVE_OK) {
			updateRet.put(collection, result);
			
			handler.handle(Future.succeededFuture(null));
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
	private void doMockRemoveOne(String collection, Handler<AsyncResult<Void>> handler) {
		Integer res = removeOneRes.get(collection);
		
		if(res == null || res == DELETE_OK) {
			removeOneRet.put(collection, true);
			
			handler.handle(Future.succeededFuture(null));
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
	private void doMockRemove(String collection, Handler<AsyncResult<Void>> handler) {
		Integer res = removeRes.get(collection);
		
		if(res == null || res == DELETE_OK) {
			removeRet.put(collection, true);
			
			handler.handle(Future.succeededFuture(null));
		} else {
			handler.handle(Future.failedFuture(new Exception("Error")));
		}
	}
	
}
