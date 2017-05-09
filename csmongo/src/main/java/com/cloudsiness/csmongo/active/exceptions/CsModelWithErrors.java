package com.cloudsiness.csmongo.active.exceptions;

public class CsModelWithErrors extends Exception {
	
	private static final long serialVersionUID = 5690191651166354307L;

	public CsModelWithErrors() {
		
	}

    public CsModelWithErrors(String message) {
       super(message);
    }
    
}
