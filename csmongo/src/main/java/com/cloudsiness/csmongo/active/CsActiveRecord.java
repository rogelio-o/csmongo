package com.cloudsiness.csmongo.active;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bson.types.ObjectId;

import com.cloudsiness.csmongo.active.annotations.CsModel;
import com.cloudsiness.csmongo.active.annotations.NotSave;
import com.cloudsiness.csmongo.active.exceptions.CsCanNotSaveAttributesOfNewRecords;
import com.cloudsiness.csmongo.active.exceptions.CsModelNotAnnotatedException;
import com.cloudsiness.csmongo.criteria.CsCriteria;
import com.cloudsiness.csmongo.data.provider.CsDbDataProvider;
import com.cloudsiness.csmongo.data.provider.CsPagination;
import com.cloudsiness.csmongo.factory.CsMongoFactory;
import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

/**
 * The classes that extend this abstract class will be a model and will have the functionalities needed in a active record model.
 * <p>
 * It is required to annotate the model classes with CsModel(collectionName=[,mongoClient=]). By default, the mongo client
 * will be the main mogo client.
 * 
 * @author 		Rogelio R. Orts Cansino
 * @version 	0.1
 * @param <T>	The type of the superclass (the class that extends the abstract class).
 */
public abstract class CsActiveRecord<T extends CsActiveRecord<T>> extends CsActiveBase<T> {
	
	
	// ATTRIBUTES -------
	
	/**
	 * The ID of the document of the model.
	 */
	public ObjectId _id;
	
	/**
	 * The Class of the supper class. Needed to get the annotations.
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> modelType = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	
	protected JsonObject searchDefaultSort;
	
	protected int searchDefaultPageSize;
	
	
	// CONSTRUCTORS -------
	
	/**
	 * Default constructor. Sets the field isNewRecord to true. Initializes the hooks containers.
	 */
	public CsActiveRecord() {
		super();
		
		this.searchDefaultSort = new JsonObject()
				.put("_id", 1);
		this.searchDefaultPageSize = CsPagination.DEFAULT_PAGE_SIZE;
	}
	
	
	// HELPER METHODS -------
	
	/**
	 * Tries to get the CsModel annotation of the superclass and if it is not possible throws an exception.
	 * 
	 * @return									The CsModel annotation of the superclass.
	 * @throws CsModelNotAnnotatedException		If the superclass has not been annotated with CsModel.
	 */
	private CsModel getModelAnnotation() throws CsModelNotAnnotatedException  {
		CsModel ann = modelType.getAnnotation(CsModel.class);
		if(ann == null)
			throw new CsModelNotAnnotatedException("The model " + modelType.getName() + "has not been annotated.");
		
		return ann;
	}
	
	/**
	 * Extracts the name of the collection of the model from the CsModel annotation (collections are conceptually like tables in relational databases).
	 * 
	 * @return									The name of the collection.
	 * @throws CsModelNotAnnotatedException		If the superclass has not been annotated with CsModel.
	 */
	private String getCollectionName() throws CsModelNotAnnotatedException {
		return getModelAnnotation().collectionName();
	}
	
	/**
	 * Extracts the name of the mongo client of the model from the CsModel annotation and gets it from the mongo factory.
	 * 
	 * @return									The mongo client.
	 * @throws CsModelNotAnnotatedException		If the superclass has not been annotated with CsModel.
	 * @throws CsClientNotInitializedException	If the mongo client has not been initializated yet.
	 */
	private MongoClient getMongoClient() throws CsModelNotAnnotatedException, CsClientNotInitializedException {
		String clientKey = getModelAnnotation().mongoClient();
		
		return CsMongoFactory.mongo(clientKey);
	}
	
	
	// DB FIND METHODS -------
	
	/**
	 * Transforms the async result of type List<JsonObject> into async result of type List<T> and calls the handler passed in the parameters.
	 * 
	 * @param res		the async result to transform.
	 * @param handler	the handler to run.
	 */
	protected void onFindAll(AsyncResult<List<JsonObject>> res, Handler<AsyncResult<List<T>>> handler) {
		if(res.succeeded()) {
			try {
				initializeModels(res.result(), res2 -> {
					if(res2.failed()) {
						handler.handle(Future.failedFuture(res2.cause()));
					} else {
						handler.handle(Future.succeededFuture(res2.result()));
					}
				});
			} catch (Exception e) {
				handler.handle(Future.failedFuture(e));
			}
		} else {
			handler.handle(Future.failedFuture(res.cause()));
		}
	}
	
	/**
	 * Transform the async result of type JsonObject into async result of type T and calls the handler passed in the parameters.
	 * 
	 * @param res		the async result to transform.
	 * @param handler	the handler to run.
	 */
	private void onFind(AsyncResult<JsonObject> res, Handler<AsyncResult<T>> handler) {
		if(res.succeeded()) {
			if(res.result() == null) {
				handler.handle(Future.succeededFuture());
			} else {
				try {
					initializeModel(res.result(), res2 -> {
						if(res2.failed()) {
							handler.handle(Future.failedFuture(res2.cause()));
						} else {
							handler.handle(Future.succeededFuture(res2.result()));
						}
					});
				} catch(Exception e) {
					handler.handle(Future.failedFuture(e));
				}
			}
		} else {
			handler.handle(Future.failedFuture(res.cause()));
		}
	}
	
	/**
	 * Retrieves and transforms a DB query, with options.
	 * 
	 * @see MongoClient#findWithOptions
	 */
	public void findAll(JsonObject query, FindOptions options, Handler<AsyncResult<List<T>>> handler) {
		try {
			if(options != null)
				getMongoClient().findWithOptions(getCollectionName(), query, options, res -> {
					onFindAll(res, handler); 
				});
			else
				getMongoClient().find(getCollectionName(), query, res -> {
					onFindAll(res, handler);
				});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	/**
	 * Retrieves and transforms a DB query.
	 * 
	 * @see MongoClient#find
	 */
	public void findAll(JsonObject query, Handler<AsyncResult<List<T>>> handler) {
		findAll(query, null, handler);
	}
	
	/**
	 * Retrieves and transform a DB query, with a criteria.
	 * 
	 * @see #findAll(JsonObject, FindOptions, Handler)
	 */
	public void findAll(CsCriteria criteria, Handler<AsyncResult<List<T>>> handler) {
		findAll(criteria.getQuery(), criteria.getOptions(), handler);
	}
	
	/**
	 * @see #findAll(JsonObject, Handler)
	 */
	public void findAll(Handler<AsyncResult<List<T>>> handler) {
		findAll(new JsonObject(), handler);
	}
	
	/**
	 * Returns a list of the model with the IDs listed in the parameters.
	 * 
	 * @see #findAll(JsonObject, Handler)
	 */
	public void findAllByPks(List<String> ids, Handler<AsyncResult<List<T>>> handler) {
		if(ids.isEmpty()) {
			handler.handle(Future.succeededFuture(new ArrayList<T>()));
			return;
		}
		
		JsonArray idsArray = new JsonArray();
		for(String id : ids) {
			idsArray.add(new JsonObject()
					.put("_id", new JsonObject()
							.put("$oid", id)));
		}
		
		JsonObject query = new JsonObject()
				.put("$or", idsArray);
		
		findAll(query, handler);
	}
	
	/**
	 * Retrieves only one document and transforms it into a model.
	 * 
	 * @see MongoClient#findOne
	 */
	public void find(JsonObject query, Handler<AsyncResult<T>> handler) {
		try {
			getMongoClient().findOne(getCollectionName(), query, null, res -> {
				onFind(res, handler);
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	public void find(CsCriteria criteria, Handler<AsyncResult<T>> handler) {
		FindOptions options = criteria.getOptions();
		options.setLimit(1);
		options.setSkip(0);
		
		try {
			getMongoClient().findWithOptions(getCollectionName(), criteria.getQuery(), options, res -> {
				if(res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
					return;
				}
				
				List<JsonObject> results = res.result();
				
				if(results.size() < 1) {
					handler.handle(Future.succeededFuture(null));
					return;
				}
				
				onFind(Future.succeededFuture(results.get(0)), handler); 
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	/**
	 * Find a model by its primary key.
	 * 
	 * @see MongoClient#findOne
	 */
	public void findByPk(String id, Handler<AsyncResult<T>> handler) {
		JsonObject query = new JsonObject().put("_id", id);
		
		find(query, handler);
	}
	
	/**
	 * Get the different values for a field into a collection.
	 * 
	 * @param field			the name of the field.
	 * @param fieldClass	the class of the results.
	 * @param handler		to handle the results.
	 */
	public void findDiffValues(String field, String fieldClass, Handler<AsyncResult<JsonArray>> handler) {
		try {
			getMongoClient().distinct(getCollectionName(), field, fieldClass, handler);
		} catch (CsModelNotAnnotatedException | CsClientNotInitializedException e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	
	// DB COUNT METHODS -------
	
	/**
	 * @see MongoClient.count
	 */
	public void count(JsonObject query, Handler<AsyncResult<Long>> handler) {
		try {
			callOnBeforeCount(hookRes -> {
				if(hookRes.failed()) {
					handler.handle(Future.failedFuture(hookRes.cause()));
				} else {
					if(hookRes.result()) {
						try {
							getMongoClient().count(getCollectionName(), query, res -> {
								try {
									if(res.succeeded()) {
										callOnAfterCount(hookRes2 -> {
											if(hookRes2.failed()) {
												handler.handle(Future.failedFuture(hookRes2.cause()));
											} else {
												handler.handle(Future.succeededFuture(res.result()));
											}
										});
									} else {
										handler.handle(Future.failedFuture(res.cause()));
									}
								} catch (Exception e) {
									handler.handle(Future.failedFuture(e));
								}
							});
						} catch (Exception e) {
							handler.handle(Future.failedFuture(e));
						}
					} else {
						handler.handle(Future.failedFuture("callOnBeforeCount false"));
					}
				}
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	/**
	 * @see #count(JsonObject, Handler)
	 */
	public void count(CsCriteria criteria, Handler<AsyncResult<Long>> handler) {
		count(criteria.getQuery(), handler);
	}
	
	/**
	 * @see #count(JsonObject, Handler)
	 */
	public void count(Handler<AsyncResult<Long>> handler) {
		count(new JsonObject(), handler);
	}
	
	// DB EXISTS METHODS -------
	
	/**
	 * Check if a document exists. A document exists if the counting result is higher than 0. 
	 * 
	 * @see #count(JsonObject, Handler)
	 */
	public void exists(JsonObject query, Handler<AsyncResult<Boolean>> handler) {
		try {
			count(query, res -> {
				if(res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				} else {
					Boolean result = (res.result() > 0);
					
					handler.handle(Future.succeededFuture(result));
				}
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	/**
	 * @see #exists(JsonObject, Handler)
	 */
	public void exists(CsCriteria criteria, Handler<AsyncResult<Boolean>> handler) {
		exists(criteria.getQuery(), handler);
	}
	
	/**
	 * @see #exists(JsonObject, Handler)
	 */
	public void exists(Handler<AsyncResult<Boolean>> handler) {
		exists(new JsonObject(), handler);
	}
	
	
	// DB SAVE METHODS -------
	
	private JsonObject getAttributesToSave() throws JsonProcessingException {
		JsonObject result = getAttributes(null, false, true);
		
		for(Field f : getFields(true, getScenario())) {
			if(f.isAnnotationPresent(NotSave.class)) {
				result.remove(f.getName());
			}
		}
		
		return result;
	}
	
	/**
	 * Saves the current model and fires the "on before save" and "on after save" events.
	 * 
	 * @param handler	handler to be executed after the query.
	 */
	public void save(Handler<AsyncResult<Boolean>> handler) {
		validate(validateRes -> {
			if(validateRes.failed()) {
				handler.handle(Future.failedFuture(validateRes.cause()));
			} else {
				if(validateRes.result()) {
					try {
						callOnBeforeSave(hookRes -> {
							if(hookRes.failed()) {
								handler.handle(Future.failedFuture(hookRes.cause()));
							} else {
								if(hookRes.result()) {
									try {
										JsonObject document = getAttributesToSave();
										
										getMongoClient().save(getCollectionName(), document, res -> {
											if(res.failed()) {
												handler.handle(Future.failedFuture(res.cause()));
											} else {
												if(res.result() != null)
													this._id = new ObjectId(res.result());
												
												try {
													if(res.succeeded()) {
														callOnAfterSave(hookRes2 -> {
															if(hookRes2.failed()) {
																handler.handle(Future.failedFuture(hookRes2.cause()));
															} else {
																this.isNewRecord = false;
																
																if(hookRes2.result()) {
																	handler.handle(Future.succeededFuture(true));
																} else {
																	handler.handle(Future.succeededFuture(false));
																}
															}
														});
													}
												} catch (Exception e) {
													handler.handle(Future.failedFuture(e));
												}	
											}
										});
									} catch (Exception e) {
										handler.handle(Future.failedFuture(e));
									}
								} else {
									handler.handle(Future.succeededFuture(false));
								}
							}
						});
					} catch (Exception e) {
						handler.handle(Future.failedFuture(e));
					}
				} else {
					handler.handle(Future.succeededFuture(false));
				}
			}
		});
	}
	
	/**
	 * Saves only the attributes listed in the parameters. Returns failure to the handler if the model is a new record.
	 * 
	 * @param attributes	the attributes that will be saved.
	 * @param handler		the handler to run after the save process.
	 */
	public void saveAttributes(List<String> attributes, Handler<AsyncResult<Void>> handler) {
		if(!isNewRecord) {
			JsonObject query = new JsonObject()
					.put("_id", new JsonObject()
							.put("$oid", _id.toString()));
			
			saveAttributes(attributes, query, handler);
		} else {
			handler.handle(Future.failedFuture(new CsCanNotSaveAttributesOfNewRecords()));
		}
	}
	
	public void saveAttributes(List<String> attributes, JsonObject query, Handler<AsyncResult<Void>> handler) {
		if(!isNewRecord) {
			try {
				JsonObject update = new JsonObject().put("$set", getAttributes(attributes));
				
				getMongoClient().updateCollection(getCollectionName(), query, update, res -> {
					if(res.failed()) {
						handler.handle(Future.failedFuture(res.cause()));
						return;
					}
					
					handler.handle(Future.succeededFuture());
				});
			} catch(Exception e) {
				handler.handle(Future.failedFuture(e));
			}
		} else {
			handler.handle(Future.failedFuture(new CsCanNotSaveAttributesOfNewRecords()));
		}
	}
	
	/**
	 * Update the attributes in update for all the documents matching with query.
	 * 
	 * @param query		the condition to update the document
	 * @param update	the attributes that will be saved.
	 * @param handler	the handler to run after the update process.
	 */
	public void updateAll(JsonObject query, JsonObject update, Handler<AsyncResult<Void>> handler) {
		update = new JsonObject().put("$set", update);
		
		updateAllFree(query, update, handler);
	}
	
	public void updateAllFree(JsonObject query, JsonObject update, Handler<AsyncResult<Void>> handler) {
		try {
			UpdateOptions options = new UpdateOptions();
			options.setMulti(true);
			getMongoClient().updateCollectionWithOptions(getCollectionName(), query, update, options, res -> {
				if(res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
					return;
				}
				
				handler.handle(Future.succeededFuture());
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	
	// DB REMOVE METHODS -------
	
	/**
	 * Removes the current model and fires the "on before delete" and on "after delete methods".
	 * 
	 * @param handler	handler to be executed after the deletion.
	 */
	public void delete(Handler<AsyncResult<Boolean>> handler) {
		JsonObject query = new JsonObject().put("_id", new JsonObject()
				.put("$oid", this._id.toString()));
		
		try {
			callOnBeforeDelete(hookRes -> {
				if(hookRes.failed()) {
					handler.handle(Future.failedFuture(hookRes.cause()));
				} else {
					if(hookRes.result()) {
						try {
							getMongoClient().removeDocument(getCollectionName(), query, res -> {
								try {
									if(res.succeeded()) {
										callOnAfterDelete(hookRes2 -> {
											if(hookRes2.failed()) {
												handler.handle(Future.failedFuture(hookRes2.cause()));
											} else {
												if(hookRes2.result()) {
													handler.handle(Future.succeededFuture(true));
												} else {
													handler.handle(Future.succeededFuture(false));
												}
											}
										});
									} else {
										handler.handle(Future.failedFuture(res.cause()));
									}
								} catch (Exception e) {
									handler.handle(Future.failedFuture(e));
								}
							});
						} catch (Exception e) {
							handler.handle(Future.failedFuture(e));
						}
					} else {
						handler.handle(Future.succeededFuture(false));
					}
				}
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	/**
	 * Removes all the documents in the collection that match with the query.
	 * 
	 * @param query		the query for to match.
	 * @param handler	the handler executed after delete the documents.
	 */
	public void deleteAll(JsonObject query, Handler<AsyncResult<Void>> handler) {
		if(query.isEmpty()) {
			drop(handler);
		} else {
			try {
				getMongoClient().removeDocuments(getCollectionName(), query, res -> {
					if(res.failed()) {
						handler.handle(Future.failedFuture(res.cause()));
						return;
					}
					
					handler.handle(Future.succeededFuture());
				});
			} catch(Exception e) {
				handler.handle(Future.failedFuture(e));
			}
		}
	}
	
	/**
	 * @see #removeAll(JsonObject, Handler)
	 */
	public void deleteAll(CsCriteria criteria, Handler<AsyncResult<Void>> handler) {
		deleteAll(criteria.getQuery(), handler);
	}
	
	private void drop(Handler<AsyncResult<Void>> handler) {
		try {
			getMongoClient().dropCollection(getCollectionName(), handler);
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	public void deleteByPk(String id, Handler<AsyncResult<Void>> handler) {
		deleteAll(new JsonObject().put("_id", new JsonObject().put("$oid", id)), handler);
	}
	
	public void deleteAfterFind(JsonObject query, Handler<AsyncResult<Boolean>> handler) {
		deleteAfterFind(query, null, handler);
	}
	
	public void deleteAfterFind(JsonObject query, String scenario, Handler<AsyncResult<Boolean>> handler) {
		find(query, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			T model = res.result();
			
			if(model == null) {
				handler.handle(Future.succeededFuture(true));
				return;
			}
			
			if(scenario != null) {
				model.setScenario(scenario);
			}
			
			model.delete(handler);
		});
	}

	public void deleteAfterFindByPk(String id, Handler<AsyncResult<Boolean>> handler) {
		deleteAfterFindByPk(id, null, handler);
	}
	
	public void deleteAfterFindByPk(String id, String scenario, Handler<AsyncResult<Boolean>> handler) {
		findByPk(id, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			T model = res.result();
			
			if(model == null) {
				handler.handle(Future.succeededFuture(true));
				return;
			}
			
			if(scenario != null) {
				model.setScenario(scenario);
			}
			
			model.delete(handler);
		});
	}
	
	/**
	 * Find all the models and then remove one by one each model. Thus, all the delete hooks 
	 * are executed. If we use the remove function of mongo client, the delete hooks aren't executed.
	 * 
	 * @param query	the query for to match.
	 * @param handler	the handler executed after delete the documents. Returns a list of
	 * 					not deleted documents ID.
	 */
	@SuppressWarnings("rawtypes")
	public void deleteAllAfterFindAll(JsonObject query, String scenario, Handler<AsyncResult<List<T>>> handler) {
		findAll(query, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			List<T> models = res.result();
			List<T> notDeleted = new ArrayList<T>();
			
			if(models.size() > 0) {
				List<Future> futures = new LinkedList<Future>();
				for(T model : models) {
					Future<Boolean> future = Future.future();
					futures.add(future);
					if(scenario != null)
						model.setScenario(scenario);
					model.delete(future.completer());
				}
				
				CompositeFuture compositeFuture = CompositeFuture.all(futures);
				compositeFuture.setHandler(finalRes -> {
					if(finalRes.failed()) {
						handler.handle(Future.failedFuture(finalRes.cause()));
					} else {
						for(int i = 0; i < futures.size(); i++) {
							Boolean r = compositeFuture.resultAt(i);
							if(!r)
								notDeleted.add(models.get(i));
						}
						
						handler.handle(Future.succeededFuture(notDeleted));
					}
				});
			} else {
				handler.handle(Future.succeededFuture(notDeleted));
			}
		});
	}
	
	public void deleteAllAfterFindAll(JsonObject query, Handler<AsyncResult<List<T>>> handler) {
		deleteAllAfterFindAll(query, null, handler);
	}
	
	/**
	 * @see #deleteAllAfterFindAll(JsonObject, Handler)
	 */
	public void deleteAllAfterFindAll(CsCriteria criteria, Handler<AsyncResult<List<T>>> handler) {
		deleteAllAfterFindAll(criteria.getQuery(), handler);
	}
	
	/**
	 * Remove attributes from the document.
	 * 
	 * @param attributes	attributes to remove.
	 * @param handler		handle to execute after the db call.
	 */
	public void deleteAttributes(List<String> attributes, Handler<AsyncResult<Void>> handler) {
		if(!isNewRecord) {
			JsonObject query = new JsonObject()
					.put("_id", new JsonObject()
							.put("$oid", _id.toString()));
			
			deleteAttributes(attributes, query, handler);
		} else {
			handler.handle(Future.failedFuture(new CsCanNotSaveAttributesOfNewRecords()));
		}
	}
	
	public void deleteAttributes(List<String> attributes, JsonObject query, Handler<AsyncResult<Void>> handler) {
		try {
			JsonObject attributesJson = new JsonObject();
			for(String attribute : attributes)
				attributesJson.put(attribute, "");
			JsonObject update = new JsonObject().put("$unset", attributesJson);
			
			getMongoClient().updateCollection(getCollectionName(), query, update, res -> {
				if(res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
					return;
				}
				
				handler.handle(Future.succeededFuture());
			});
		} catch(Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	
	// AGGREGATE -------
	
	public void aggregate(JsonArray pipeline, Handler<AsyncResult<JsonArray>> handler) {
		try {
			JsonObject command = new JsonObject()
					  .put("aggregate", getCollectionName())
					  .put("pipeline", pipeline);
			
			getMongoClient().runCommand("aggregate", command, res -> {
				if (res.succeeded()) {
				    JsonArray result = res.result().getJsonArray("result");
				    handler.handle(Future.succeededFuture(result));
				} else {
				    res.cause().printStackTrace();
				}
			});
		} catch (CsModelNotAnnotatedException | CsClientNotInitializedException e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	
	// GETERS AND SETERS -------
	
	@Override
	public String toString() {
		return modelType.getClass().getSimpleName() + (_id != null ? ":" + _id.toString() : "");
	}
	
	
	// SEARCH
	
	public CsDbDataProvider<T> search(MultiMap queryParams) {
		return search(queryParams, getSearchDefaultPageSize());
	}
	
	public CsDbDataProvider<T> search(MultiMap queryParams, Integer pageSize) {
		return search(queryParams, pageSize, getSearchDefaultSort());
	}
	
	public CsDbDataProvider<T> search(MultiMap queryParams, Integer defaultPageSize, JsonObject defaultSort) {
		CsCriteria criteria = new CsCriteria();
		
		String filters = queryParams.get("filters");
		String sort = queryParams.get("sort");
		Integer pageSize = queryParams.get("pageSize") != null ? Integer.valueOf(queryParams.get("pageSize")) : defaultPageSize;
		
		if(filters != null) {
			criteria.setQuery(searchFilters(new JsonObject(filters)));
		}
		
		if(sort != null) {
			criteria.setSort(searchSort(new JsonObject(sort)));
		} else if(defaultSort != null) {
			criteria.setSort(defaultSort);
		}
		
		CsDbDataProvider<T> dataProvider = new CsDbDataProvider<T>(modelType, criteria);
		dataProvider.getPagination().setPageSize(searchPageSize(pageSize));
		dataProvider.getPagination().setCurrentPage(queryParams);
		
		return dataProvider;
	}
	
	protected JsonObject searchSort(JsonObject sort) {
		return sort;
	}
	
	protected int searchPageSize(Integer pageSize) {
		return pageSize != null ? pageSize : 10;
	}
	
	public JsonObject getSearchDefaultSort() {
		return searchDefaultSort;
	}
	
	public int getSearchDefaultPageSize() {
		return searchDefaultPageSize;
	}
	
	
	/* EQUALS ------- */
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CsActiveRecord<?> other = (CsActiveRecord<?>) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
	
}
