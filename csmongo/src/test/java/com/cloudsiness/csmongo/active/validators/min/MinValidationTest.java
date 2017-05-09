package com.cloudsiness.csmongo.active.validators.min;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MinValidationTest {

	private MinValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new MinValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenTheAttributeIsLowerOrEqualThanTheMax(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigInteger bd = new BigInteger("1001");
		model.attribute = new BigDecimal(bd);
		model.attribute2 = bd;
		model.attribute3 = bd.toByteArray();
		model.attribute4 = bd.shortValue();
		model.attribute5 = bd.longValue();
		
		
		validator.isValid(model,"attribute", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model,"attribute2", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model,"attribute3", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model,"attribute4", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model,"attribute5", res -> {
			context.assertNull(res.result());
		});
		model.attribute5 = 1000L;
		validator.isValid(model,"attribute5", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenTheAttributeIsHigherThanTheMax(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigInteger bd = new BigInteger("999");
		model.attribute = new BigDecimal(bd);
		model.attribute2 = bd;
		model.attribute3 = bd.toByteArray();
		model.attribute4 = bd.shortValue();
		model.attribute5 = bd.longValue();
		
		
		validator.isValid(model,"attribute", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model,"attribute2", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model,"attribute3", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model,"attribute4", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model,"attribute5", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenTheAttributeIsEqualThanTheMaxAndInclusiveIsFalse(TestContext context) throws NoSuchFieldException, SecurityException, CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		initializeValidator("notInclusive");
		
		HelperClass model = new HelperClass();
		model.notInclusive = 1000L;
		
		validator.isValid(model, "notInclusive", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void throwsExceptionIsTheTypeOfTheAttributeIsNotValid(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attributeWrong = 0.5;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator(String fildName) throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField(fildName).getAnnotation(Min.class));
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute");
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Min(value=1000L)
		public BigDecimal attribute;
		@Min(value=1000L)
		public BigInteger attribute2;
		@Min(value=1000L)
		public byte[] attribute3;
		@Min(value=1000L)
		public Short attribute4;
		@Min(value=1000L)
		public Long attribute5;
		
		@Min(value=1000L, inclusive=false)
		public Long notInclusive;
		
		@Min(value=1000L)
		public double attributeWrong;
	}

}
