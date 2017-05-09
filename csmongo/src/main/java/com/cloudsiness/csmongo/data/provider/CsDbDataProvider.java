package com.cloudsiness.csmongo.data.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.criteria.CsCriteria;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Provides data in terms of CsActiveRecord paginated and filtered.
 * 
 * @author 		Rogelio R. Orts Cansino
 * @version	 	0.1
 * @param <T>	the class of the provided data.
 */
public class CsDbDataProvider<T extends CsActiveRecord<T>> implements CsDataProvider<T> {
	
	/**
	 * The class of the provided models.
	 */
	private Class<T> modelClass;
	
	/**
	 * The criteria to filter the data.
	 */
	private CsCriteria criteria;
	
	/**
	 * The pagination to paginate the data.
	 */
	private CsPagination<T> pagination;
	
	
	public CsDbDataProvider(Class<T> modelClass, CsCriteria criteria, CsPagination<T> pagination) {
		this.modelClass = modelClass;
		this.criteria = criteria;
		this.pagination = pagination;
	}
	
	public CsDbDataProvider(Class<T> modelClass, CsCriteria criteria) {
		this(modelClass, criteria, new CsPagination<T>(modelClass));
	}
	
	
	/**
	 * Gets the data with the criteria and the pagination restrinctions and invoke the handler with the data obtained.
	 * 
	 * @param handler	the handler to invoke.
	 */
	@Override
	public void getData(Handler<AsyncResult<List<T>>> handler) {
		pagination.initialize(criteria, res -> {
			if(res.succeeded()) {
				try {
					modelClass.getConstructor().newInstance().findAll(pagination.paginateCriteria(criteria), handler);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					handler.handle(Future.failedFuture(e));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}
	
	
	@Override
	public CsCriteria getCriteria() {
		return criteria;
	}
	
	@Override
	public CsDbDataProvider<T> setCriteria(CsCriteria criteria) {
		this.criteria = criteria;
		
		return this;
	}

	public CsPagination<T> getPagination() {
		return pagination;
	}

	public void setPagination(CsPagination<T> pagination) {
		this.pagination = pagination;
	}
	
	@Override
	public int getPageSize() {
		return pagination.getPageSize();
	}
	
	@Override
	public int getCurrentPage() {
		return pagination.getCurrentPage();
	}
	
	@Override
	public Integer getPageCount() {
		return pagination.getPageCount();
	}
	
	@Override
	public boolean hasPreviousPage() {
		return pagination.hasPreviousPage();
	}
	
	@Override
	public boolean hasNextPage() {
		return pagination.hasNextPage();
	}
	
	
	public void setLastPageByDefault(MultiMap queryParams, Handler<AsyncResult<Void>> handler) {
		pagination.setLastPageByDefault(queryParams, criteria.getQuery(), handler);
	}

	@Override
	public Long getItemCount() {
		return pagination.getItemCount();
	}

	@Override
	public CsDataProvider<T> setJoinQuery(String attribute, List<String> attributes) {
		JsonObject query = criteria.getQuery();
		
		if(attribute == "_id") {
			JsonArray ids = new JsonArray();
			attributes.forEach(attr -> {
				ids.add(new JsonObject().put("$oid", attr));
			});
			query.put(attribute, new JsonObject()
					.put("$in", ids));
		} else {
			query.put(attribute, new JsonObject()
					.put("$in", attributes));
		}
		
		criteria.setQuery(query);
		
		return this;
	}

	@Override
	public CsDataProvider<T> setCurrentPage(int currentPage) {
		pagination.setCurrentPage(currentPage);
		
		return this;
	}
	
	
	public static <T extends CsActiveRecord<T>> CsDbDataProvider<T> getTextSearchDataProvider(String q, JsonObject query, Class<T> clazz) {
		if(query == null)
			query = new JsonObject();
		
		CsCriteria criteria = new CsCriteria();
		criteria.setQuery(query
			.put("$text", new JsonObject()
				.put("$search", q)
			)
		);
		criteria.setSort(new JsonObject()
			.put("score", new JsonObject().put("$meta", "textScore"))
		);
		criteria.getOptions().setFields(new JsonObject()
			.put("score", new JsonObject().put("$meta", "textScore"))
		);
		
		return new CsDbDataProvider<T>(clazz, criteria);
	}
	
}
