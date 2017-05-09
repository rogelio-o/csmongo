package com.cloudsiness.csmongo.helpers.serializers;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ObjectIdJsonDeserializer extends JsonDeserializer<ObjectId> {

	@Override
	public ObjectId deserialize(JsonParser j, DeserializationContext s)
			throws IOException, JsonProcessingException {
		if(j == null)
			return null;
		
		JsonNode node = j.getCodec().readTree(j);
		if(node == null)
			return null;

		JsonNode oidNode = node.get("$oid");
		if(oidNode == null) {
			return new ObjectId(node.asText());
		}
		
		String id = oidNode.asText();
		
		if(id == null)
			return null;
		
		return new ObjectId(id);
	}

}
