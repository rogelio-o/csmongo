package com.cloudsiness.csmongo.active.validators.max;

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
public class DecimalMaxValidationTest {
	
	private DecimalMaxValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new DecimalMaxValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenTheAttributeIsLowerOrEqualThanTheMax(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigDecimal bd = new BigDecimal("1000.8989");
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
	}
	
	@Test
	public void returnsTheMessageWhenTheAttributeIsHigherThanTheMax(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigDecimal bd = new BigDecimal("1000.8991");
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
	public void returnsTheMessageWhenTheAttributeIsEqualThanTheMaxAndInclusiveIsFalse(TestContext context) throws NoSuchFieldException, SecurityException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
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
		model.attributeWrong = true;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator(String fildName) throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField(fildName).getAnnotation(DecimalMax.class));
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute");
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@DecimalMax(value="1000.8990")
		public BigDecimal attribute;
		@DecimalMax(value="1000.8990")
		public String attribute2;
		@DecimalMax(value="1000.8990")
		public Double attribute3;
		@DecimalMax(value="1000.8990")
		public Float attribute4;
		
		@DecimalMax(value="1000.8990", inclusive=false)
		public String notInclusive;
		
		@DecimalMax(value="1000.8990")
		public boolean attributeWrong;
	}
}
