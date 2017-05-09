package com.cloudsiness.csmongo.active.validators.pattern;

import java.util.function.Function;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * /**
 * The annotated {@code CharSequence} must match the specified regular expression.
 * The regular expression follows the Java regular expression conventions
 * see {@link java.util.regex.Pattern}.
 * <p/>
 * Accepts {@code CharSequence}. {@code null} elements are considered valid.
 * 
 * @author 	Rogelio R. Orts Cansino
 * @version	0.1
 */
public class PatternValidation implements CsConstraintValidator<Pattern> {
	
	private String message;
	private String regexp;
	private int[] flags;

	@Override
	public void initialize(Pattern constraintAnnotation) {
		message = constraintAnnotation.message();
		flags = constraintAnnotation.flags();
		regexp = constraintAnnotation.regexp();
	}
	
	public void initialize(String message, String regexp, int[] flags) {
		this.message = message == null || message.isEmpty() ? Pattern.MESSAGE : message;
		this.regexp = regexp;
		this.flags = flags;
	}

	@Override
	public void isValid(CsActiveForm<?> model, String attribute, Handler<AsyncResult<CsValidationError>> handler) {
		if(attribute == null) {
			handler.handle(Future.failedFuture(new CsConstraintNotValidInModel()));
			return;
		}
		
		Object o = model.getAttributeValue(attribute);
		
		isValid(o, message -> new CsValidationError(message, attribute).setValue(regexp), handler);
	}
	
	public void isValid(Object o, Handler<AsyncResult<CsValidationError>> handler) {
		isValid(o, message -> new CsValidationError(message), handler);
	}
	
	public void isValid(Object o, Function<String, CsValidationError> func, Handler<AsyncResult<CsValidationError>> handler) {
		if(o == null) {
			handler.handle(Future.succeededFuture());
			return;
		} else if(o instanceof CharSequence) {
			CharSequence c = (CharSequence) o;
			
			if(getCompiledPattern().matcher(c.toString()).matches()) {
				handler.handle(Future.succeededFuture());
				return;
			}
			
		} else {
			handler.handle(Future.failedFuture(new CsValidatorNotValidForAttributeType("Only CharSequence attributes are supported on Pattern validator.")));
			return;
		}
			
		handler.handle(Future.succeededFuture(func.apply(message)));
	}
	
	private java.util.regex.Pattern getCompiledPattern() {
		java.util.regex.Pattern p;
		Integer flag = getFlags();
		
		if(flag != null)
			p = java.util.regex.Pattern.compile(regexp, flag);
		else
			p = java.util.regex.Pattern.compile(regexp);
		
		return p;
	}
	
	private Integer getFlags() {
		Integer flag = null;
		if(flags.length > 0)
			flag = flags[0];
		
		if(flags.length > 1) {
			for(int i = 1; i < flags.length; i++) {
				flag = flag | flags[i];
			}
		}
		
		return flag;
	}

}
