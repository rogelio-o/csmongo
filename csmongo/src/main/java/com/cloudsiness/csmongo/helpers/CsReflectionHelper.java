package com.cloudsiness.csmongo.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CsReflectionHelper {

	
	public static CsReflectionHelper helper() {
		return new CsReflectionHelper();
	}
	
	
	public Class<?> resolveParameterizedType(Field f, Class<?> c) {
		if(c.getGenericSuperclass() != null && c.getGenericSuperclass().getClass().isAssignableFrom(ParameterizedType.class)) {
			ParameterizedType t = (ParameterizedType) c.getGenericSuperclass();
			
			for(Type type : t.getActualTypeArguments()) {
				if(f.getType().isAssignableFrom((Class<?>) type))
					return (Class<?>) type;
			}
		}
		
		return f.getType();
	}
	
}
