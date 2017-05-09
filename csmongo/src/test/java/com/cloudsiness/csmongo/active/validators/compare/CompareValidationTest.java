package com.cloudsiness.csmongo.active.validators.compare;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CompareValidationTest {
	
	private CompareValidation validator;
	
	@Before
	public void setUp() {
		validator = new CompareValidation();
	}

	@Test
	public void ifStringIsEqualsToString(TestContext context) throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute");
		HelperClass model = new HelperClass();
		model.attribute = "test";
		model.attribute2 = "test";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void ifStringIsDifferentThantString(TestContext context) throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute");
		HelperClass model = new HelperClass();
		model.attribute = "test";
		model.attribute2 = "test2";
		
		validator.isValid(model, "attribute", res -> {
			context.assertNotNull(res.result());
			
			model.attribute = null;
			validator.isValid(model, "attribute", res2 -> {
				context.assertNotNull(res2.result());
			});
		});
	}
	
	@Test
	public void ifStringIsNullAndOtherAlsoNull(TestContext context) throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute");
		HelperClass model = new HelperClass();
		model.attribute = null;
		model.attribute2 = null;
		
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void ifIntIsEqualsToInt(TestContext context) throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute3");
		HelperClass model = new HelperClass();
		model.attribute3 = 1;
		model.attribute4 = 1;
		
		validator.isValid(model, "attribute3", res -> {
			context.assertNull(res.result());
		});
	}
	
	@Test
	public void ifIntIsDifferentThantString(TestContext context) throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute3");
		HelperClass model = new HelperClass();
		model.attribute3 = 1;
		model.attribute4 = 5;
		
		validator.isValid(model, "attribute3", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	@Test
	public void ifIsOfDifferentType(TestContext context) throws NoSuchFieldException, SecurityException {
		initializeValidator("attribute4");
		HelperClass model = new HelperClass();
		model.attribute = "5";
		model.attribute4 = 5;
		
		validator.isValid(model, "attribute4", res -> {
			context.assertNotNull(res.result());
		});
	}
	
	private void initializeValidator(String field) throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField(field).getAnnotation(Compare.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Compare(attribute="attribute2")
		public String attribute;
		public String attribute2;
		
		@Compare(attribute="attribute4")
		public int attribute3;
		@Compare(attribute="attribute")
		public int attribute4;
		
	}
}
