package com.cloudsiness.csmongo.active.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.factory.CsMongoFactory;

import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CsModel {
	String collectionName();
	String mongoClient() default CsMongoFactory.MAIN;
}
