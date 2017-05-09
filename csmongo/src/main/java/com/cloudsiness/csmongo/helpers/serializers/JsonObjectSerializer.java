package com.cloudsiness.csmongo.helpers.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;

import io.vertx.core.json.JsonObject;

public class JsonObjectSerializer extends JsonSerializer<JsonObject> {
	
	private RawSerializer<String> rawSerializer = new RawSerializer<String>(String.class);

	@Override
	public void serialize(JsonObject o, JsonGenerator j, SerializerProvider s)
			throws IOException, JsonProcessingException {
		if(o == null) {
            j.writeNull();
        } else {
        	rawSerializer.serialize(o.toString(), j, s);
        }
	}

}
