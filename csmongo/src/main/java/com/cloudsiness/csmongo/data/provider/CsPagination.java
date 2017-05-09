package com.cloudsiness.csmongo.data.provider;

import java.lang.reflect.InvocationTargetException;

import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.criteria.CsCriteria;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/**
 * This class provides methods to paginate data.
 * 
 * @author 		Rogelio R. Orts Cansino
 * @version 	0.1
 * @param <T>	the class of the paginated data.
 */
public class CsPagination<T extends CsActiveRecord<T>> {
	
	public static final String DFAULT_PAGE_PARAM = "page";
	
	public static final int DEFAULT_PAGE_SIZE = 10;
	
	private Class<T> modelClass;
	
	/**
	 * The zero-based index of the current page.
	 */
	private int currentPage = 0;
	
	/**
	 * Total number of items.
	 */
	private Long itemCount;
	
	/**
	 * Number of pages.
	 */
	private Integer pageCount;
	
	/**
	 * 	Number of items in each page.
	 */
	private int pageSize;
	
	/**
	 * The parameter containing the page number.
	 */
	private String pageParam;
	
	
	public CsPagination(Class<T> modelClass, int pageSize, int currentPage, String pageParam) {
		this(modelClass, pageSize, currentPage);
		this.pageParam = pageParam;
	}
	
	public CsPagination(Class<T> modelClass, int pageSize, int currentPage) {
		this(modelClass, pageSize);
		this.currentPage = currentPage;
	}
	
	public CsPagination(Class<T> modelClass, int currentPage) {
		this.modelClass = modelClass;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.currentPage = currentPage;
	}
	
	public CsPagination(Class<T> modelClass) {
		this.modelClass = modelClass;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.pageParam = DFAULT_PAGE_PARAM;
	}
	
	
	/**
	 * Initializes the pagination class. Gets the total num of item in the collection and calculates the number of pages.
	 * <p>
	 * Before this method is called, the pagination object has itemCount and pageCount attributes with null values null.
	 * 
	 * @param query		query to filter the data in the count process.
	 * @param handler	handler to be invoked after the count process.
	 */
	public void initialize(JsonObject query, Handler<AsyncResult<Void>> handler) {
		initialize(query, false, handler); 
	}
	
	public void update(JsonObject query, Handler<AsyncResult<Void>> handler) {
		initialize(query, true, handler); 
	}
	
	private void initialize(JsonObject query, boolean redone, Handler<AsyncResult<Void>> handler) {
		if(itemCount == null || redone) {
			try {
				modelClass.getConstructor().newInstance().count(query, res -> {
					if(res.succeeded()) {
						itemCount = res.result();
						pageCount = pageSize == -1 ? 1 : (int) Math.ceil(Double.valueOf(itemCount) / pageSize);
						handler.handle(Future.succeededFuture());
					} else {
						handler.handle(Future.failedFuture(res.cause()));
					}
				});
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				handler.handle(Future.failedFuture(e));
			}
		} else {
			handler.handle(Future.succeededFuture());
		}
	}
	
	/**
	 * @see #initialize(JsonObject, Handler)
	 */
	public void initialize(CsCriteria criteria, Handler<AsyncResult<Void>> handler) {
		initialize(criteria.getQuery(), handler);
	}
	
	/**
	 * Set the limit and skip fields of criteria to do the pagination on database call.
	 * 
	 * @param criteria	the criteria to transform.
	 * @return			the criteria transformed.
	 */
	public CsCriteria paginateCriteria(CsCriteria criteria) {
		criteria.setLimit(pageSize);
		criteria.setSkip(pageSize == -1 ? 0 : currentPage * pageSize);
		
		return criteria;
	}
	
	public void setLastPageByDefault(MultiMap queryParams, JsonObject query, Handler<AsyncResult<Void>> handler) {
		initialize(query, false, res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}
			
			if(queryParams.get(getPageParam()) == null) {
				currentPage = pageCount - 1;
			}
			
			handler.handle(Future.succeededFuture());
		});
	}
	
	/**
	 * Sums one because the internal field is zero-based.
	 */
	public int getCurrentPage() {
		return currentPage + 1;
	}
	
	/**
	 * Reduces or sums one because the internal field is zero-based. Also, if the page is lower than one or higher than pageCount, will be set as the max or min.
	 */
	public void setCurrentPage(int currentPage) {
		if(currentPage < 1)
			this.currentPage = 0;
		else if(pageCount != null && currentPage > pageCount)
			this.currentPage = pageCount - 1;
		else
			this.currentPage = currentPage - 1;
	}
	
	public void setCurrentPage(MultiMap params) {
		String page = params.get(pageParam);
		
		if(page != null) {
			try { 
				setCurrentPage(Integer.valueOf(page));
			} catch(NumberFormatException e) {}
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Long getItemCount() {
		return itemCount;
	}
	
	public CsPagination<T> setItemCount(Long itemCount) {
		this.itemCount = itemCount;
		
		return this;
	}

	public Integer getPageCount() {
		return pageCount;
	}
	
	public CsPagination<T> setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
		
		return this;
	}
	
	public String getPageParam() {
		return pageParam;
	}

	public void setPageParam(String pageParam) {
		this.pageParam = pageParam;
	}

	/**
	 * Sums two to obtain the nextPage because the internal field is zero-based. If does not exist next page, returns null.
	 */
	public Integer nextPage() {
		if(!hasNextPage())
			return null;
		else
			return currentPage + 2;
	}
	
	/**
	 * Does not subtract anything to obtain the previousPage because the internal field is zero-based. If does not exist next page, returns null.
	 */
	public Integer previousPage() {
		if(!hasPreviousPage())
			return null;
		else
			return currentPage;
	}
	
	public boolean hasNextPage() {
		return pageCount != null && (currentPage + 1) < pageCount;
	}
	
	public boolean hasPreviousPage() {
		return currentPage > 0;
	}
	
}
