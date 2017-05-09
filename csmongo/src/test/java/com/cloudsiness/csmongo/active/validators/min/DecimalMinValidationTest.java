package com.cloudsiness.csmongo.active.validators.min;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class DecimalMinValidationTest {

	private DecimalMinValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new DecimalMinValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenTheAttributeIsHigherOrEqualThanTheMin(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigDecimal bd = new BigDecimal("1000.8991");
		model.attribute = bd;
		model.attribute2 = bd.toString();
		model.attribute3 = bd.doubleValue();
		model.attribute4 = bd.floatValue();
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute2", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute3", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute4", res -> {
			context.assertNull(res.result());
		});
		model.attribute2 = "1000.8990";
		validator.isValid(model, "attribute2", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenTheAttributeIsLowerThanTheMin(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigDecimal bd = new BigDecimal("1000.8989");
		model.attribute = bd;
		model.attribute2 = bd.toString();
		model.attribute3 = bd.doubleValue();
		model.attribute4 = bd.floatValue();
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute2", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute3", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute4", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenTheAttributeIsEqualThanTheMinAndInclusiveIsFalse(TestContext context) throws NoSuchFieldException, SecurityException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		initializeValidator("notInclusive");
		
		HelperClass model = new HelperClass();
		model.notInclusive = "1000.8990";
		
		validator.isValid(model, "notInclusive", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void throwsExceptionIsTheTypeOfTheAttributeIsNotValid(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attributeWrong = 1;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator(String fildName) throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField(fildName).getAnnotation(DecimalMin.class));
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute");
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@DecimalMin(value="1000.8990")
		public BigDecimal attribute;
		@DecimalMin(value="1000.8990")
		public String attribute2;
		@DecimalMin(value="1000.8990")
		public Double attribute3;
		@DecimalMin(value="1000.8990")
		public Float attribute4;
		
		@DecimalMin(value="1000.8990", inclusive=false)
		public String notInclusive;
		
		@DecimalMin(value="1000.8990")
		public Integer attributeWrong;
		
	}

}
