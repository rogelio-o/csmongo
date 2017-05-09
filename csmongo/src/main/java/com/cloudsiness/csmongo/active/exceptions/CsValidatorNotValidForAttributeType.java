package com.cloudsiness.csmongo.active.exceptions;

public class CsValidatorNotValidForAttributeType extends Exception {
	
	private static final long serialVersionUID = 3018527887445966444L;

	public CsValidatorNotValidForAttributeType() {
		
	}

    public CsValidatorNotValidForAttributeType(String message) {
       super(message);
    }
    
}
