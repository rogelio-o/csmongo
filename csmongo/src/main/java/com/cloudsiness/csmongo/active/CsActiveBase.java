package com.cloudsiness.csmongo.active;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cloudsiness.csmongo.active.annotations.events.AfterCount;
import com.cloudsiness.csmongo.active.annotations.events.AfterDelete;
import com.cloudsiness.csmongo.active.annotations.events.AfterFind;
import com.cloudsiness.csmongo.active.annotations.events.AfterSave;
import com.cloudsiness.csmongo.active.annotations.events.BeforeCount;
import com.cloudsiness.csmongo.active.annotations.events.BeforeDelete;
import com.cloudsiness.csmongo.active.annotations.events.BeforeFind;
import com.cloudsiness.csmongo.active.annotations.events.BeforeSave;
import com.cloudsiness.csmongo.active.exceptions.CsModelsNotInitialized;
import com.cloudsiness.csmongo.active.hooks.CsHookHandler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public abstract class CsActiveBase<T extends CsActiveBase<T>> extends CsActiveForm<T> {

	// CONSTANTS -------
	
	public static final String SCENARIO_CREATE = "scenarioCreate";
	public static final String SCENARIO_UPDATE = "scenarioUpdate";
	public static final String SCENARIO_DELETE = "scenarioDelete";
	public static final String SCENARIO_SEARCH = "scenarioSearch";
	
	
	/**
	 * Constant name for event "on before save".
	 */
	public static final String ON_BEFORE_SAVE = "onBeforeSave";
	
	/**
	 * Constant name for event "on after save".
	 */
	public static final String ON_AFTER_SAVE = "onAfterSave";
	
	/**
	 * Constant name for event "on before find".
	 */
	public static final String ON_BEFORE_FIND = "onBeforeFind";
	
	/**
	 * Constant name for event "on after find".
	 */
	public static final String ON_AFTER_FIND = "onAfterFind";
	
	/**
	 * Constant name for event "on before count".
	 */
	public static final String ON_BEFORE_COUNT = "onBeforeCount";
	
	/**
	 * Constant name for event "on after count".
	 */
	public static final String ON_AFTER_COUNT = "onAfterCount";
	
	/**
	 * Constant name for event "on before delete".
	 */
	public static final String ON_BEFORE_DELETE = "onBeforeDelete";
	
	/**
	 * Constant name for event "on after delete".
	 */
	public static final String ON_AFTER_DELETE = "onAfterDelete";
	
	
	// ATTRIBUTES -------
	
	/**
	 * Saves if the model is a new record.
	 */
	protected boolean isNewRecord;
	
	
	// HOOKS FIELDS -------
	
	/**
	 * Hooks registered for to be called on before save.
	 */
	private List<CsHookHandler<T>> hooksOnBeforeSave;
	
	/**
	 * Hooks registered for to be called on after save.
	 */
	private List<CsHookHandler<T>> hooksOnAfterSave;
	
	/**
	 * Hooks registered for to be called on before find.
	 */
	private List<CsHookHandler<T>> hooksOnBeforeFind;
	
	/**
	 * Hooks registered for to be called on after find.
	 */
	private List<CsHookHandler<T>> hooksOnAfterFind;
	
	/**
	 * Hooks registered for to be called on before count.
	 */
	private List<CsHookHandler<T>> hooksOnBeforeCount;
	
	/**
	 * Hooks registered for to be called on after count.
	 */
	private List<CsHookHandler<T>> hooksOnAfterCount;
	
	/**
	 * Hooks registered for to be called on before delete.
	 */
	private List<CsHookHandler<T>> hooksOnBeforeDelete;
	
	/**
	 * Hooks registered for to be called on after delete.
	 */
	private List<CsHookHandler<T>> hooksOnAfterDelete;
	
	
	// CONSTRUCTORS -------
	
	protected CsActiveBase() {
		super();
		
		this.isNewRecord = true;
		
		this.hooksOnBeforeSave = new LinkedList<CsHookHandler<T>>();
		this.hooksOnAfterSave = new LinkedList<CsHookHandler<T>>();
		this.hooksOnBeforeFind = new LinkedList<CsHookHandler<T>>();
		this.hooksOnAfterFind = new LinkedList<CsHookHandler<T>>();
		this.hooksOnBeforeCount = new LinkedList<CsHookHandler<T>>();
		this.hooksOnAfterCount = new LinkedList<CsHookHandler<T>>();
		this.hooksOnBeforeDelete = new LinkedList<CsHookHandler<T>>();
		this.hooksOnAfterDelete = new LinkedList<CsHookHandler<T>>();
	}
	
	
	// ON EVENT METHODS -------
	
	/**
	 * Call the methods annotated with @BeforeSave and the hooks of the field hooksOnBeforeSave.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnBeforeSave(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(BeforeSave.class, hooksOnBeforeSave, handler);
	}
	
	/**
	 * Call the methods annotated with @AfterSave and the hooks of the field hooksOnAfterSave.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnAfterSave(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(AfterSave.class, hooksOnAfterSave, handler);
	}
	
	/**
	 * Call the methods annotated with @BeforeFind and the hooks of the field hooksOnBeforeFind.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnBeforeFind(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(BeforeFind.class, hooksOnBeforeFind, handler);
	}
	
	/**
	 * Call the methods annotated with @AfterFind and the hooks of the field hooksOnAfterFind.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnAfterFind(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(AfterFind.class, hooksOnAfterFind, handler);
	}
	
	/**
	 * Call the methods annotated with @BeforeCount and the hooks of the field hooksOnBeforeCount.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnBeforeCount(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(BeforeCount.class, hooksOnBeforeCount, handler);
	}
	
	/**
	 * Call the methods annotated with @AfterCount and the hooks of the field hooksOnAfterCount.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnAfterCount(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(AfterCount.class, hooksOnAfterCount, handler);
	}
	
	/**
	 * Call the methods annotated with @BeforeDelete and the hooks of the field hooksOnBeforeDelete.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnBeforeDelete(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(BeforeDelete.class, hooksOnBeforeDelete, handler);
	}
	
	/**
	 * Call the methods annotated with @AfterDelete and the hooks of the field hooksOnAfterDelete.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnAfterDelete(Handler<AsyncResult<Boolean>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callOn(AfterDelete.class, hooksOnAfterDelete, handler);
	}
	
	/**
	 * Register hooks to an event.
	 * 
	 * @param where		the event where register the hook.
	 * @param handler	the handler for to run when the event happen.
	 */
	@Override
	public void registerHook(String where, CsHookHandler<T> handler) {
		switch(where) {
			case ON_BEFORE_SAVE:
				hooksOnBeforeSave.add(handler);
				break;
			case ON_AFTER_SAVE:
				hooksOnAfterSave.add(handler);
				break;
			case ON_BEFORE_FIND:
				hooksOnBeforeFind.add(handler);
				break;
			case ON_AFTER_FIND:
				hooksOnAfterFind.add(handler);
				break;
			case ON_BEFORE_COUNT:
				hooksOnBeforeCount.add(handler);
				break;
			case ON_AFTER_COUNT:
				hooksOnAfterCount.add(handler);
				break;
			case ON_BEFORE_DELETE:
				hooksOnBeforeDelete.add(handler);
				break;
			case ON_AFTER_DELETE:
				hooksOnAfterDelete.add(handler);
				break;
			default:
				super.registerHook(where, handler);
		}
	}
	
	
	// AUX -------
	
	/**
	 * Sets the values of the attributes of the model with the values in the JSON object listed in the parameters.
	 * Triggers the beforeFind and AfterFind events.
	 * 
	 * @param values	the values of the attributes.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InstantiationException 
	 */
	protected void initializeModel(JsonObject values, Handler<AsyncResult<T>> handler) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException{
		T result = modelType.getConstructor().newInstance();
		
		result.callOnBeforeFind(res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			try {
				result.setAttributes(false, values);
				result.isNewRecord = false;
			
				result.callOnAfterFind(res2 -> {
					if(res2.failed()) {
						handler.handle(Future.failedFuture(res2.cause()));
						return;
					}
					
					handler.handle(Future.succeededFuture(result));
				});
			} catch (Exception e) {
				handler.handle(Future.failedFuture(e));
			}
		});
	}
	
	/**
	 * Auxiliar method to initialize models recursively. It is mandatory to initialize models 
	 * in order because the sort.
	 * 
	 * @param count
	 * @param results
	 * @param objects
	 * @param handler
	 */
	@SuppressWarnings("unchecked")
	protected void initializeModelsAux(int count, List<T> results, List<JsonObject> objects, Handler<AsyncResult<List<T>>> handler) {
		Handler<AsyncResult<T>> resultHandler = res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
			} else {
				results.add(res.result());
				int newCount = count + 1;
				
				if(newCount < objects.size()) {
					initializeModelsAux(newCount, results, objects, handler);
				} else {
					handler.handle(Future.succeededFuture(results));
				}
			}
		};
		
		try {
			JsonObject model = objects.get(count) instanceof JsonObject ? objects.get(count) : new JsonObject((Map<String, Object>) objects.get(count));
			initializeModel(model, resultHandler);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException
				| NoSuchMethodException | SecurityException e) {
			handler.handle(Future.failedFuture(new CsModelsNotInitialized()));
		}
	}
	
	/**
	 * Transforms a list of JsonObjects into a list of models.
	 * 
	 * @param objects					the list of JsonObjects.
	 * @return							the list of models.
	 * @throws CsModelsNotInitialized	if some errors happen when initializing any model.
	 */
	protected void initializeModels(List<JsonObject> objects, Handler<AsyncResult<List<T>>> handler) throws CsModelsNotInitialized {
		List<T> results = new LinkedList<T>();
		
		if(objects.size() > 0) {
			initializeModelsAux(0, results, objects, handler);
		} else {
			handler.handle(Future.succeededFuture(results));
		}
	}
	
	
	// GETERS AND SETERS -------
	
	public boolean isNewRecord() {
		return isNewRecord;
	}
	
}
