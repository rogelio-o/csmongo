package com.cloudsiness.csmongo.active.validators.past;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PastValidationTest {


	private PastValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new PastValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenTheAttributeDateIsInThePast(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -1);
		
		model.attribute = c.getTime();
		model.attribute2 = c;
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
		validator.isValid(model, "attribute2", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void returnsTheMessageWhenTheAttributeDateIsInTheFuture(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 1);
		
		model.attribute = c.getTime();
		model.attribute2 = c;
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
		validator.isValid(model, "attribute2", res -> {
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
	public void throwsAnExceptionIsTheTypeOfTheAttributeIsNotCalendarNeitherDate(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attributeWrong = true;
		
		validator.isValid(model, "attributeWrong", res -> {
			context.assertEquals(res.cause().getClass(), CsValidatorNotValidForAttributeType.class);
		});
	}
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Past.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Past
		public Date attribute;
		@Past
		public Calendar attribute2;
		
		@Past
		public boolean attributeWrong;
		
	}

}
