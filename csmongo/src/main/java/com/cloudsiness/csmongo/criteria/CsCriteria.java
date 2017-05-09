package com.cloudsiness.csmongo.criteria;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

public class CsCriteria {
	
	private JsonObject query;
	
	private FindOptions options;
	
	public CsCriteria() {
		query = new JsonObject();
		options = new FindOptions();
	}
	
	public CsCriteria(JsonObject query, FindOptions options) {
		this.query = query;
		this.options = options;
	}
	
	public CsCriteria addCondition(String key, Object value) {
		query.put(key, value);
		
		return this;
	}
	
	public CsCriteria removeCondition(String key) {
		query.remove(key);
		
		return this;
	}
	
	public JsonObject getQuery() {
		return query;
	}
	
	public CsCriteria setFields(JsonObject fields) {
		options.setFields(fields);
		
		return this;
	}
	
	public JsonObject getFields() {
		return options.getFields();
	}
	
	private JsonObject getFieldsNeverNull() {
		JsonObject fields = getFields();
		if(fields == null)
			fields = new JsonObject();
		
		return fields;
	}
	
	public CsCriteria addField(String field) {
		JsonObject fields = getFieldsNeverNull();
		fields.put(field, true);
		setFields(fields);
		
		return this;
	}
	
	public CsCriteria removeField(String field) {
		JsonObject fields = getFieldsNeverNull();
		fields.remove(field);
		setFields(fields);
		
		return this;
	}
	
	public CsCriteria setSort(JsonObject sort) {
		options.setSort(sort);
		
		return this;
	}
	
	public CsCriteria setSort(String field, int value) {
		options.setSort(new JsonObject().put(field, value));
		
		return this;
	}
	
	public JsonObject getSort() {
		return options.getSort();
	}
	
	public CsCriteria setLimit(int limit) {
		options.setLimit(limit);
		
		return this;
	}
	
	public CsCriteria setQuery(JsonObject query) {
		this.query = query;
		
		return this;
	}
	
	public int getLimit() {
		return options.getLimit();
	}
	
	public CsCriteria setSkip(int skip) {
		options.setSkip(skip);
		
		return this;
	}
	
	public int getSkip() {
		return options.getSkip();
	}
	
	public FindOptions getOptions() {
		return options;
	}
}
