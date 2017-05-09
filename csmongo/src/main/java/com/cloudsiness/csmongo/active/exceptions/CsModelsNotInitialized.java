package com.cloudsiness.csmongo.active.exceptions;

public class CsModelsNotInitialized extends Exception {
	
	private static final long serialVersionUID = -5571937269585199144L;

	public CsModelsNotInitialized() {
		
	}

    public CsModelsNotInitialized(String message) {
       super(message);
    }

}
