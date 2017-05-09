package com.cloudsiness.csmongo.factory.exceptions;

/**
 * Exceptions that will happen when the factory is asked for a non initialized mongo client.
 *  
 * @author 	Rogelio R. Orts Cansino.
 * @version 0.1 
 */
public class CsClientNotInitializedException extends Exception {
	
	private static final long serialVersionUID = -450490901504980536L;

	public CsClientNotInitializedException() {
		
	}

    public CsClientNotInitializedException(String message) {
       super(message);
    }
    
}
