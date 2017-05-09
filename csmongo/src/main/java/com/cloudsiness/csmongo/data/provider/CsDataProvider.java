package com.cloudsiness.csmongo.data.provider;

import java.util.List;

import com.cloudsiness.csmongo.criteria.CsCriteria;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface CsDataProvider<T> {
	
	public void getData(Handler<AsyncResult<List<T>>> handler);
	
	public CsCriteria getCriteria();
	
	public CsDataProvider<T> setCriteria(CsCriteria criteria);
	
	public int getPageSize();
	
	public int getCurrentPage();
	
	public CsDataProvider<T> setCurrentPage(int currentPage);
	
	public Integer getPageCount();
	
	public Long getItemCount();
	
	public boolean hasPreviousPage();
	
	public boolean hasNextPage();
	
	public CsDataProvider<T> setJoinQuery(String attribute, List<String> attributes);
	
}
