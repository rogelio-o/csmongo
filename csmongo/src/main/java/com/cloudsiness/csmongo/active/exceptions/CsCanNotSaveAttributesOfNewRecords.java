package com.cloudsiness.csmongo.active.exceptions;

public class CsCanNotSaveAttributesOfNewRecords extends Exception {
	
	private static final long serialVersionUID = -432002599612774534L;

	public CsCanNotSaveAttributesOfNewRecords() {
		
	}

    public CsCanNotSaveAttributesOfNewRecords(String message) {
       super(message);
    }
}
