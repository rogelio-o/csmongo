package com.cloudsiness.csmongo.active.validators.required;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class RequiredValidationTest {
	
	private RequiredValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new RequiredValidation();
		initializeValidator();
	}
	
	@Test
	public void returnsNullWhenAttributeIsNotNullAndIsNotEmptyString(TestContext context) throws CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "test";
		model.attribute2 = 1;
		model.attribute3 = true;
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute2", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute3", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenAttributeIsNullOrIsAEmptyString(TestContext context) throws CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute2", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute3", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Required.class));
	}

	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Required
		public String attribute;
		@Required
		public Integer attribute2;
		@Required
		public Boolean attribute3;
		
	}

}
