package com.cloudsiness.csmongo.active.validators.bool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;
import com.cloudsiness.csmongo.active.validators.CsValidationError;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class AssertTrueValidationTest {

	private HelperClass helper;
	private AssertTrueValidation validator;
	
	@Before
	public void setUp() {
		helper = new HelperClass();
		validator = new AssertTrueValidation();
	}

	@Test
	public void returnMessageWhenBooleanAttributeIsFalse(TestContext context) throws CsValidatorNotValidForAttributeType, NoSuchFieldException, SecurityException, CsConstraintNotValidInModel {
		helper.attribute = false;
		initializeValidator("attribute");
		
		validator.isValid(helper, "attribute", res -> {
			CsValidationError validationError = res.result();
			
			context.assertNotNull(validationError);
			context.assertEquals(validationError.getMessage(), "test message");
			context.assertEquals(validationError.getAttribute(), "attribute"); 
		});
	}
	
	@Test
	public void returnNullWhenBooleanAttributeIsTrue(TestContext context) throws CsValidatorNotValidForAttributeType, NoSuchFieldException, SecurityException, CsConstraintNotValidInModel {
		helper.attribute = true;
		initializeValidator("attribute");
		
		validator.isValid(helper, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	
	@Test
	public void throwsExceptionWhenTypeOfTheAttributeIsNotBoolean(TestContext context) throws NoSuchFieldException, SecurityException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		helper.attributeNotCorrect = "test";
		initializeValidator("attributeNotCorrect");
		
		validator.isValid(helper, "attributeNotCorrect", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	@Test
	public void ifAttributeIsNullAlwaysReturnsTheMessage(TestContext context) throws NoSuchFieldException, SecurityException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		helper.attribute = null;
		initializeValidator("attribute");
		validator.isValid(helper, "attribute", res -> {
			context.assertNotNull(res.result());
		});
		
		helper.attributeNotCorrect = null;
		initializeValidator("attributeNotCorrect");
		validator.isValid(helper, "attributeNotCorrect", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	private void initializeValidator(String fieldName) throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField(fieldName).getAnnotation(AssertTrue.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@AssertTrue(message="test message")
		public Boolean attribute;
		
		@AssertTrue
		public String attributeNotCorrect;
		
	}

}
