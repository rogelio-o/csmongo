package com.cloudsiness.csmongo.active.validators.digits;

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
public class DigitsValidationTest {
	
	private DigitsValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new DigitsValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenAttributeIsInTheRange(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigDecimal bd = new BigDecimal("100.50");
		model.attribute = bd;
		model.attribute2 = bd.toBigInteger();
		model.attribute3 = bd.toString();
		model.attribute4 = bd.toBigInteger().toByteArray();
		model.attribute5 = bd.shortValue();
		model.attribute6 = bd.intValue();
		model.attribute7 = bd.longValue();
		model.attribute8 = bd.doubleValue();
		model.attribute9 = bd.floatValue();
		
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
		validator.isValid(model, "attribute5", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute6", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute7", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute8", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute9", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenIntegerIsNotInTheRange(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = new BigDecimal("1000.50");
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenFranctionIsNotInTheRange(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = new BigDecimal("100.333");
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenBothIsNotInTheRange(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		BigDecimal bd = new BigDecimal("1000.333");
		model.attribute = bd;
		model.attribute2 = bd.toBigInteger();
		model.attribute3 = bd.toString();
		model.attribute4 = bd.toBigInteger().toByteArray();
		model.attribute5 = bd.shortValue();
		model.attribute6 = bd.intValue();
		model.attribute7 = bd.longValue();
		model.attribute8 = bd.doubleValue();
		model.attribute9 = bd.floatValue();
		
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
		validator.isValid(model, "attribute5", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute6", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute7", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute8", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute9", res -> {
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
	public void throwsExceptionIfTheAttributeIsNotOfACorrectType(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attributeWrong = true;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Digits.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Digits(integer=3, fraction=2)
		public BigDecimal attribute;
		@Digits(integer=3, fraction=2)
		public BigInteger attribute2;
		@Digits(integer=3, fraction=2)
		public String attribute3;
		@Digits(integer=3, fraction=2)
		public byte[] attribute4;
		@Digits(integer=3, fraction=2)
		public Short attribute5;
		@Digits(integer=3, fraction=2)
		public Integer attribute6;
		@Digits(integer=3, fraction=2)
		public Long attribute7;
		@Digits(integer=3, fraction=2)
		public Double attribute8;
		@Digits(integer=3, fraction=2)
		public Float attribute9;
		@Digits(integer=3, fraction=2)
		public boolean attributeWrong;
		
	}
}
