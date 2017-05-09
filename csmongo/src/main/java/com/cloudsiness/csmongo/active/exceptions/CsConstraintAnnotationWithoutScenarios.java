package com.cloudsiness.csmongo.active.exceptions;

public class CsConstraintAnnotationWithoutScenarios extends Exception {
	
	private static final long serialVersionUID = 3014924916171031299L;

	public CsConstraintAnnotationWithoutScenarios(String annotationName) {
		super("The constraint annotation " + annotationName + " does not includes scenarios().");
	}

}
