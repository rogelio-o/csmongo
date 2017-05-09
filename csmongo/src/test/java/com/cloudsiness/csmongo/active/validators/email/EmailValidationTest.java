package com.cloudsiness.csmongo.active.validators.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class EmailValidationTest {
	
	private EmailValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new EmailValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenTheAttributeIsACorrectEmail(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "email@email.com";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test 
	public void returnsTheMessageWhenTheAttributeIsNotACorrectEmail(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "wrongEmail";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnsNullWhenTheAttributeIsNull(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void throwsExceptionWhenTheAttributeIsNotAString(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attributeWrong = true;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Email.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Email
		public String attribute;
		
		@Email
		public boolean attributeWrong;
	}

}
