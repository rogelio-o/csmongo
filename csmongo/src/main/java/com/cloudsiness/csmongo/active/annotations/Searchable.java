package com.cloudsiness.csmongo.active.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Searchable {

	public final int EXACT = 0;
	public final int REGEXP = 1;
	
	public int type() default EXACT;
	public String options() default "";
	
}
