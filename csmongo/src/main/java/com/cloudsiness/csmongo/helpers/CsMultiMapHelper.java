package com.cloudsiness.csmongo.helpers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.cloudsiness.csmongo.helpers.collectors.CsJsonArrayCollector;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CsMultiMapHelper {
	
	public static CsMultiMapHelper helper() {
		return new CsMultiMapHelper();
	}
	

	public JsonObject toJson(MultiMap params) {
		JsonObject body = new JsonObject();
		
		for(Entry<String, String> entry : params.entries()) {
			toJsonAttribute(entry.getKey(), entry.getValue(), body);
		}
		
		return (JsonObject) transformAuxIntoArrays(body);
	}
	
	private Object transformAuxIntoArrays(JsonObject body) {
		if(body.containsKey("$array")) {
			JsonObject child = body.getJsonObject("$array");
			
			return child.stream()
					.sorted((a, b) -> Integer.valueOf(a.getKey()) - Integer.valueOf(b.getKey()))
					.map(x -> {
						if(x.getValue() != null && x.getValue().getClass().equals(JsonObject.class)) {
							return transformAuxIntoArrays((JsonObject) x.getValue());
						} else {
							return x.getValue();
						}
					})
					.collect(new CsJsonArrayCollector());
		} else {
			for(Map.Entry<String, Object> entry : body) {
				if(entry.getValue() != null && entry.getValue().getClass().equals(JsonObject.class)) {
					body.put(entry.getKey(), transformAuxIntoArrays((JsonObject) entry.getValue()));
				}
			}
			
			return body;
		}
		
	}
	
	private void toJsonAttribute(String key, String value, JsonObject body) {
		if(paramIsArray(key) || paramIsObject(key)) {
			putSpecialAttribute(key, value, body);
		} else {
			putAttribute(key, value, body);
		}
	}
	
	private void putAttribute(String key, Object value, JsonObject body) {
		if(value.getClass().equals(String.class)) {
			Boolean tryBoolean = BooleanUtils.toBooleanObject((String) value);
			if(tryBoolean != null)
				body.put(key, tryBoolean);
			else if(NumberUtils.isNumber((String) value))
				body.put(key, NumberUtils.createNumber((String) value));
			else
				body.put(key, value);
		} else {
			body.put(key, value);
		}
	}
	
	private void addAttribute(Object value, JsonObject arrayWrapper, int index) {
		JsonObject array = new JsonObject();
		arrayWrapper.put("$array", array);
		
		if(value.getClass().equals(String.class)) {
			Boolean tryBoolean = BooleanUtils.toBooleanObject((String) value);
			if(tryBoolean != null)
				array.put(String.valueOf(index), tryBoolean);
			else if(NumberUtils.isNumber((String) value))
				array.put(String.valueOf(index), NumberUtils.createNumber((String) value));
			else
				array.put(String.valueOf(index), value);
		} else {
			array.put(String.valueOf(index), value);
		}
	}
	
	private void putSpecialAttribute(String key, String value, JsonObject body) {
		Object prev = value;
		while(paramIsArray(key) || paramIsObject(key)) {
			Object next;
			if(paramIsArray(key)) {
				next = new JsonObject();
				addAttribute(prev, (JsonObject) next, getArrayIndex(key));
			} else {
				next = new JsonObject();
				putAttribute(setAttributeGetKey(key), prev, (JsonObject) next);
			}
			
			prev = next;
			key = getParamName(key);
		}
		
		merge(body, key, prev);
	}
	
	private void merge(JsonObject body, String key, Object value) {
		if(!body.containsKey(key)) {
			putAttribute(key, value, body);
		} else {
			Object b = body.getValue(key);
			
			if(b instanceof JsonObject && value instanceof JsonObject) {
				for(Map.Entry<String, Object> e : (JsonObject) value) {
					merge((JsonObject) b, e.getKey(), e.getValue());
				}
			} else if(b instanceof JsonArray && value instanceof JsonArray) {
				((JsonArray) b).addAll((JsonArray) value);
			} else {
				putAttribute(key, value, body);
			}
		}
	}
	
	private boolean paramIsArray(String key) {
		return key.matches(".+\\[[0-9]*\\]$");
	}
	
	private int getArrayIndex(String key) {
		int first = key.lastIndexOf("[");
		return Integer.valueOf(key.substring(first + 1, key.length() - 1));
	}
	
	private boolean paramIsObject(String key) {
		return key.matches(".+\\[.+?\\]$");
	}
	
	private String getParamName(String key) {
		return key.replaceFirst("\\[[^\\[]*\\]$", "");
	}
	
	private String setAttributeGetKey(String key) {
		Pattern pattern = Pattern.compile(".+\\[(.+)\\]$");
		Matcher matcher = pattern.matcher(key);
		
		if(matcher.find())
			return matcher.group(1);
		else
			return null;
	}
	
	public MultiMap toMultiMap(JsonObject params) {
		MultiMap result = MultiMap.caseInsensitiveMultiMap();
		
		toMultiMap(result, "", params);
		
		return result;
	}
	
	private String getMultiMapKey(String prefix, String name) {
		if(prefix.isEmpty())
			return name;
		else
			return prefix + (name.isEmpty() ? "" : "[" + name + "]");
	}
	
	private void toMultiMap(MultiMap result, String prefix, JsonObject params) {
		for(Map.Entry<String, Object> param : params) {
			if(param.getValue() != null) {
				if(param.getValue().getClass().equals(JsonObject.class)) {
					toMultiMap(result, getMultiMapKey(prefix, param.getKey()), (JsonObject) param.getValue());
				} else if(param.getValue().getClass().equals(JsonArray.class)) {
					toMultiMap(result, prefix, param.getKey(), (JsonArray) param.getValue());
				} else {
					result.add(getMultiMapKey(prefix, param.getKey()), param.getValue().toString());
				}
			}
		}
	}
	
	private void toMultiMap(MultiMap result, String prefix, String name, JsonArray params) {
		int i = 0;
		for(Object p : params) {
			if(p != null) {
				String key = getMultiMapKey(prefix, name) + "[" + i + "]";
				if(p.getClass().equals(JsonObject.class)) {
					toMultiMap(result, key, (JsonObject) p);
				} else if(p.getClass().equals(JsonArray.class)) {
					toMultiMap(result, key, "", (JsonArray) p);
				} else {
					result.add(key, p.toString());
				}
				i++;
			}
		}
	}
	
}
