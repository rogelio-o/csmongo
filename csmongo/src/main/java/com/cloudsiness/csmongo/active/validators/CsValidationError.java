package com.cloudsiness.csmongo.active.validators;

public class CsValidationError {

	private String message;
	
	private String attribute;
	
	private String attribute2;
	
	private String attribute3;
	
	private String value;
	
	private String value2;
	
	
	public CsValidationError(String message) {
		this.message = message;
	}
	
	public CsValidationError(String message, String attribute) {
		this(message);
		
		this.attribute = attribute;
	}
	
	public CsValidationError(String message, String attribute, String attribute2) {
		this(message, attribute);
		
		this.attribute2 = attribute2;
	}
	
	public CsValidationError(String message, String attribute, String attribute2, String attribute3) {
		this(message, attribute, attribute2);
		
		this.attribute3 = attribute3;
	}
	

	public String getMessage() {
		return message;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getAttribute2() {
		return attribute2;
	}

	public String getAttribute3() {
		return attribute3;
	}
	
	public String getValue() {
		return value;
	}
	
	public CsValidationError setValue(String value) {
		this.value = value;
		
		return this;
	}

	public String getValue2() {
		return value2;
	}
	
	public CsValidationError setValue2(String value2) {
		this.value2 = value2;
		
		return this;
	}
	
}
