package com.cloudsiness.csmongo.active.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cloudsiness.csmongo.active.CsActiveForm;

/**
 * Attributes with this annotation can be set from a POST request.
 * 
 * @author 	Rogelio R. Orts Cansino
 * @version	0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Safe {
	String[] scenarios() default {CsActiveForm.MAIN_SCENARIO};
}
