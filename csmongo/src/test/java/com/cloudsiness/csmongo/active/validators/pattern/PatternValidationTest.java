package com.cloudsiness.csmongo.active.validators.pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PatternValidationTest {
	
	private PatternValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new PatternValidation();
		initializeValidator();
	}
	
	@Test
	public void returnsNullIfTheAttributeAndThePatternMatch(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "abcdef";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageIfTheAttributeAndThePatternDoNotMatch(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "a1b2c3";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnsNullIfTheAttributeIsNull(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}

	@Test
	public void throwsExceptionIfAttributeIsNotAString(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attributeWrong = true;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Pattern.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Pattern(regexp = "^[A-Za-z]+$")
		public String attribute;
		
		@Pattern(regexp = "^[A-Za-z]+$")
		public boolean attributeWrong;
	}

}
