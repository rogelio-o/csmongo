package com.cloudsiness.csmongo.active.exceptions;

public class CsModelNotAnnotatedException extends Exception {

	private static final long serialVersionUID = -7840921760089930383L;
	
	public CsModelNotAnnotatedException() {
		
	}

    public CsModelNotAnnotatedException(String message) {
       super(message);
    }

}
