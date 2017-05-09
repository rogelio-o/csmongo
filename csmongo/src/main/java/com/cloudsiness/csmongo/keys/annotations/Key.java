package com.cloudsiness.csmongo.keys.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Key {
	String name();
	String type();
	boolean unique() default false;
	int weight() default -1;
	long expireAfterSeconds() default -1;
}
