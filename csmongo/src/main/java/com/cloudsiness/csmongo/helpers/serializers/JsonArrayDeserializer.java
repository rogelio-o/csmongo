package com.cloudsiness.csmongo.helpers.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import io.vertx.core.json.JsonArray;

public class JsonArrayDeserializer extends JsonDeserializer<JsonArray> {

	@Override
	public JsonArray deserialize(JsonParser j, DeserializationContext s)
			throws IOException, JsonProcessingException {
		if(j == null)
			return null;
		
		JsonNode node = j.getCodec().readTree(j);
		if(node == null)
			return null;
		
		return new JsonArray(node.toString());
	}
	
}
