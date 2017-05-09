package com.cloudsiness.csmongo.helpers.serializers;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.BeanProperty.Std;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CsMapDeserializer extends JsonDeserializer<Map<Object,Object>> implements ContextualDeserializer {
	
	private BeanProperty property;
	
	
	public CsMapDeserializer() {
		
	}
	
	public CsMapDeserializer(BeanProperty property) {
		this.property = property;
	}
	
	
	@Override
    public Map<Object,Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		ObjectCodec codec = p.getCodec();
        TreeNode node = codec.readTree(p);
        
        Map<Object, Object> result = new LinkedHashMap<Object, Object>();
        if(node != null) {
        	BeanProperty valueProperty = new Std(property.getFullName(), property.getType().containedType(1), property.getFullName(), null, 
    				property.getMember(), property.getMetadata());
        	
	        if(node.isArray()) {
	        	int index = 0;
	        	for(TreeNode n : (ArrayNode) node) {
	        		JsonParser p2 = n.traverse(p.getCodec());
	        		if(p2.getCurrentToken() == null)
	        			p2.nextToken();
	        		
	        		result.put(
	    				ctxt.findKeyDeserializer(property.getType().containedType(0), property).deserializeKey(String.valueOf(index), ctxt), 
	    				ctxt.findContextualValueDeserializer(valueProperty.getType(), valueProperty).deserialize(p2, ctxt)
	        		);
	        		index++;
	        	}
	        } else {
	        	Iterator<String> fieldNames = node.fieldNames();
	        	while(fieldNames.hasNext()) {
	        		String fieldName = fieldNames.next();
	        		TreeNode n = node.get(fieldName);
	        		JsonParser p2 = n.traverse(codec);
	        		if(p2.getCurrentToken() == null)
	        			p2.nextToken();
	        		
	        		result.put(
	        			fieldName == null ? null : ctxt.findKeyDeserializer(property.getType().containedType(0), property).deserializeKey(fieldName, ctxt),
	        			ctxt.findContextualValueDeserializer(valueProperty.getType(), valueProperty).deserialize(p2, ctxt)
	        		);
	        	}
	        }
        }
        
        return result;
	}

	@Override
	public JsonDeserializer<Map<Object,Object>> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		return new CsMapDeserializer(property);
	}

}
