package com.cloudsiness.csmongo.helpers.serializers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;


public class DateDeserializer extends JsonDeserializer<Date> {
	
	public DateDeserializer() {
		
	}

	@Override
	public Date deserialize(JsonParser j, DeserializationContext s) throws IOException, JsonProcessingException {
		if(j == null)
			return null;
		
		try {
			Long longDate = j.getCodec().readValue(j, Long.class);
			if(longDate != null) {
				return new Date(longDate);
			}
		} catch(InvalidFormatException e1) {
			String stringDate = j.getCodec().readValue(j, String.class);
			if(stringDate != null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				try {
					return format.parse(stringDate);
				} catch (ParseException e3) {
					e3.printStackTrace();
					return null;
				}
			}
		}
		
		return null;
	}

}
