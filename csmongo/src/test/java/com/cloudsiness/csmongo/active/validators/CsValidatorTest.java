package com.cloudsiness.csmongo.active.validators;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.required.Required;
import com.cloudsiness.csmongo.active.validators.required.RequiredValidation;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CsValidatorTest {

	// FIXES
	
	private CsValidator validator;
	
	
	// SETUP
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = CsValidator.create(RequiredValidation.class, getValidatorAnnotation(), CsActiveForm.MAIN_SCENARIO);
	}
	
	
	// TEST validate
	
	@Test
	public void returnsMessageWhenErrorOnValidate(TestContext context) throws InstantiationException, IllegalAccessException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		TestClass t = new TestClass();
		
		validator.validate(t, "testField", res -> {
			CsValidationError validationError = res.result();
			
			context.assertNotNull(validationError);
			context.assertEquals(validationError.getMessage(), "test message");
			context.assertEquals(validationError.getAttribute(), "testField");
		});
	}
	
	@Test
	public void returnsNullWhenNoErrorOnValidate(TestContext context) throws InstantiationException, IllegalAccessException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		TestClass t = new TestClass();
		t.testField = "test";
		
		validator.validate(t, "testField", res -> {
			context.assertNull(res.result());
		});
	}
	
	
	// Helpers
	
	private	Annotation getValidatorAnnotation() throws NoSuchFieldException, SecurityException {
		return TestClass.class.getField("testField").getAnnotation(Required.class);
	}
	
	
	// HELPER CLASSES
	
	public class TestClass extends CsActiveForm<TestClass> {
		
		@Required(message="test message")
		public String testField;
	}
}
