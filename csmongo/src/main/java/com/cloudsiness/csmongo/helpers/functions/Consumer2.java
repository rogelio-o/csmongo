package com.cloudsiness.csmongo.helpers.functions;

@FunctionalInterface
public interface Consumer2<A, B> {
	public void accept (A a, B b);
}

