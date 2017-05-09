package com.cloudsiness.csmongo.active.exceptions;

public class CsConstraintNotValidInModel extends Exception {

	private static final long serialVersionUID = -1579605162946188113L;
	
	
	public CsConstraintNotValidInModel() {
		super("This contraint can not be applied directly in the model. It has to be applied to an attribute.");
	}
}
