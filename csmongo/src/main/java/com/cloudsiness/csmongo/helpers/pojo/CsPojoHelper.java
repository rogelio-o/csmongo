package com.cloudsiness.csmongo.helpers.pojo;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;

import com.cloudsiness.csmongo.helpers.serializers.CsMapDeserializer;
import com.cloudsiness.csmongo.helpers.serializers.DateDeserializer;
import com.cloudsiness.csmongo.helpers.serializers.JsonArrayDeserializer;
import com.cloudsiness.csmongo.helpers.serializers.JsonArraySerializer;
import com.cloudsiness.csmongo.helpers.serializers.JsonObjectDeserializer;
import com.cloudsiness.csmongo.helpers.serializers.JsonObjectSerializer;
import com.cloudsiness.csmongo.helpers.serializers.ObjectIdJsonDeserializer;
import com.cloudsiness.csmongo.helpers.serializers.ObjectIdJsonSerializer;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CsPojoHelper {
	
	public static <T> JsonObject serialize(T obj, boolean emptyValues) throws JsonProcessingException {
		return serialize(obj, emptyValues, null);
	}

	public static <T> JsonObject serialize(T obj, boolean emptyValues, SimpleModule customModule) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY);
		if(!emptyValues) {
			mapper.setSerializationInclusion(Include.NON_EMPTY);
		}
		
		SimpleModule module = new SimpleModule();
		module.addSerializer(JsonObject.class, new JsonObjectSerializer());
		module.addSerializer(JsonArray.class, new JsonArraySerializer());
		module.addSerializer(ObjectId.class, new ObjectIdJsonSerializer());
		mapper.registerModule(module);
		
		if(customModule != null)
			mapper.registerModule(customModule);
		
		return new JsonObject(mapper.writeValueAsString(obj));
	}
	
	public static <T> void deserialize(T obj, JsonObject body, SimpleModule customModule) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY);
		
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Map.class, new CsMapDeserializer());
		module.addDeserializer(JsonObject.class, new JsonObjectDeserializer());
		module.addDeserializer(JsonArray.class, new JsonArrayDeserializer());
		module.addDeserializer(ObjectId.class, new ObjectIdJsonDeserializer());
		module.addDeserializer(Date.class, new DateDeserializer());
		mapper.registerModule(module);
		
		if(customModule != null)
			mapper.registerModule(customModule);
		
		ObjectReader reader = mapper.readerForUpdating(obj);
		reader.readValue(body.encodePrettily());
	}
	
	public static <T> T deserialize(JsonObject body, Class<T> clazz) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY);
		
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Map.class, new CsMapDeserializer());
		module.addDeserializer(JsonObject.class, new JsonObjectDeserializer());
		module.addDeserializer(JsonArray.class, new JsonArrayDeserializer());
		module.addDeserializer(ObjectId.class, new ObjectIdJsonDeserializer());
		module.addDeserializer(Date.class, new DateDeserializer());
		mapper.registerModule(module);
		
		ObjectReader reader = mapper.reader();
		T obj = reader.forType(clazz).readValue(body.encodePrettily());
		
		return obj;
	}
	
}
