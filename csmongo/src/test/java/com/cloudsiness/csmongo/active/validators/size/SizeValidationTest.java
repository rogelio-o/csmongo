package com.cloudsiness.csmongo.active.validators.size;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.CsActiveForm;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintNotValidInModel;
import com.cloudsiness.csmongo.active.exceptions.CsValidatorNotValidForAttributeType;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class SizeValidationTest {

	private SizeValidation validator;
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException {
		validator = new SizeValidation();
		initializeValidator();
	}
	
	@SuppressWarnings("serial")
	@Test
	public void returnsNullIfTheAttributeSizeIsBetweenTheValues(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "ab";
		model.attribute2 = new LinkedList<String>() {{ add("a"); add("b"); }};
		model.attribute3 = new HashMap<Integer, String>() {{ put(0, "a"); put(1, "b");}};
		model.attribute4 = new String[]{"a","b"};
		
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
		model.attribute = "abcd";
		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
		});
	}
	
	@SuppressWarnings("serial")
	@Test
	public void returnsTheMessageIfTheAttributeSizeIsUnderTheMin(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "a";
		model.attribute2 = new LinkedList<String>() {{ add("a"); }};
		model.attribute3 = new HashMap<Integer, String>() {{ put(0, "a");}};
		model.attribute4 = new String[]{"a"};
		
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
	
	@SuppressWarnings("serial")
	@Test
	public void returnsTheMessageIfTheAttributeSizeIsOverTheMax(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();
		model.attribute = "abcde";
		model.attribute2 = new LinkedList<String>() {{ add("a"); add("b"); add("c"); add("d"); add("e"); }};
		model.attribute3 = new HashMap<Integer, String>() {{ put(0, "a"); put(1, "b"); put(2, "c"); put(3, "d"); put(4, "e");}};
		model.attribute4 = new String[]{"a","b","c","d","e"};
		
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
	public void returnsNullIfTheAttributeIsNull(TestContext context) throws CsValidatorNotValidForAttributeType, CsConstraintNotValidInModel {
		HelperClass model = new HelperClass();

		validator.isValid(model, "attribute", res -> {
			context.assertNull(res.result());
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
	
	private void initializeValidator() throws NoSuchFieldException, SecurityException {
		validator.initialize(HelperClass.class.getField("attribute").getAnnotation(Size.class));
	}
	
	public class HelperClass extends CsActiveForm<HelperClass> {
		
		@Size(min=2, max=4)
		public String attribute;
		@Size(min=2, max=4)
		public List<String> attribute2;
		@Size(min=2, max=4)
		public Map<Integer, String> attribute3;
		@Size(min=2, max=4)
		public String[] attribute4;
		
		@Size(min=2, max=4)
		public boolean attributeWrong;
		
	}

}
