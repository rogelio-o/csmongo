package com.cloudsiness.csmongo.active.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attributes with this annotation will not be saved in the database.
 * 
 * @author 	Rogelio R. Orts Cansino
 * @version	0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotSave {
	
}
