package com.cloudsiness.csmongo.active.validators.future;

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
public class FutureValidationTest {
	
	private FutureValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new FutureValidation();
		initializeValidator();
	}

	@Test
	public void returnsNullWhenTheAttributeDateIsInTheFuture(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 1);
		
		HelperClass model = new HelperClass();
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
	public void returnsTheMessageWhenTheAttributeDateIsTodayOrInThePast(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -1);
		
		HelperClass model = new HelperClass();
		
		model.attribute = new Date();
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
		});
		
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
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Future.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Future
		public Date attribute;
		
		@Future
		public Calendar attribute2;
		
		@Future
		public boolean attributeWrong;
	}

}
