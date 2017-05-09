package com.cloudsiness.csmongo.active;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.active.annotations.Safe;
import com.cloudsiness.csmongo.active.annotations.events.AfterValidate;
import com.cloudsiness.csmongo.active.annotations.events.BeforeValidate;
import com.cloudsiness.csmongo.active.helpers.TestTypeValidator;
import com.cloudsiness.csmongo.active.helpers.TestTypeValidatorValidation;
import com.cloudsiness.csmongo.active.hooks.CsHookHandler;
import com.cloudsiness.csmongo.active.validators.CsValidator;
import com.cloudsiness.csmongo.active.validators.email.Email;
import com.cloudsiness.csmongo.active.validators.email.EmailValidation;
import com.cloudsiness.csmongo.active.validators.required.Required;
import com.cloudsiness.csmongo.active.validators.required.RequiredValidation;
import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CsActiveFormTest {
	
	// CONSTANTS
	
	
	// FIXES
	
	private boolean beforeValidateCalled;
	private boolean afterValidateCalled;
	
	private boolean hookBeforeValidateCalled;
	private boolean hookAfterValidateCalled;
	
	private boolean beforeValidateResult;
	private boolean afterValidateResult;
	
	
	// SETUP
	
	@Before
	public void setUp() {
		beforeValidateCalled = false;
		afterValidateCalled = false;
		
		hookBeforeValidateCalled = false;
		hookAfterValidateCalled = false;
		
		beforeValidateResult = true;
		afterValidateResult = true;
	}

	// TESTS - callOnBeforeValidate
	
	@Test
	public void methodsAreInvokedWhenCallOnBeforeValidate(TestContext context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnBeforeValidate", res -> {
			context.assertTrue(beforeValidateCalled);
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnBeforeValidate(TestContext context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnBeforeValidate", Form.ON_BEFORE_VALIDATE, (model, res) -> {
			hookBeforeValidateCalled = true;
			
			res.handle(Future.succeededFuture(true));
		}, res -> {
			context.assertTrue(hookBeforeValidateCalled);
		});
	}
	
	
	// TESTS - callOnAfterValidate
	
	@Test
	public void methodsAreInvokedWhenCallOnAfterValidate(TestContext context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnAfterValidate", res -> {
			context.assertTrue(afterValidateCalled);
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnAfterValidate(TestContext context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnAfterValidate", Form.ON_AFTER_VALIDATE, (model, res) -> {
			hookAfterValidateCalled = true;
			
			res.handle(Future.succeededFuture(true));
		}, res -> {
			context.assertTrue(hookAfterValidateCalled);
		});
	}
	
	
	// TEST - getFields
	
	@Test
	public void allAttributesGet(TestContext context) throws NoSuchFieldException, SecurityException {
		Form form = new Form();
		List<Field> attributes = form.getFields();
		
		context.assertTrue(attributes.contains(Form.class.getDeclaredField("attribute1")));
		context.assertTrue(attributes.contains(Form.class.getDeclaredField("safeAttribute")));
		context.assertTrue(attributes.contains(Form.class.getDeclaredField("validatedAttribute")));
	}
	
	@Test
	public void fieldsThatAreNotAttributesNotGet(TestContext context) throws NoSuchFieldException, SecurityException {
		Form form = new Form();
		
		context.assertFalse(form.getFields().contains(Form.class.getDeclaredField("notAttribute")));
	}
	
	@Test
	public void onlyGetSafeAttributesIfIsIndicated(TestContext context) throws NoSuchFieldException, SecurityException {
		Form form = new Form();
		List<Field> attributes = form.getFields(true);
		
		context.assertFalse(attributes.contains(Form.class.getDeclaredField("attribute1")));
		context.assertTrue(attributes.contains(Form.class.getDeclaredField("safeAttribute")));
		context.assertTrue(attributes.contains(Form.class.getDeclaredField("validatedAttribute")));
	}
	
	
	// TEST - getValidators
	
	@Test
	public void getAllTheValidatorsOfAField(TestContext context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		Form form = new Form();
		Method m = CsActiveForm.class.getDeclaredMethod("getValidators", Field.class, String.class);
		m.setAccessible(true);
		Field f = Form.class.getField("validatedAttribute");
		@SuppressWarnings("unchecked")
		List<CsValidator> validators = (List<CsValidator>) m.invoke(form, f, CsActiveForm.MAIN_SCENARIO);
		
		context.assertTrue(validators.contains(CsValidator.create(RequiredValidation.class, f.getAnnotation(Required.class), CsActiveForm.MAIN_SCENARIO)));
		context.assertTrue(validators.contains(CsValidator.create(EmailValidation.class, f.getAnnotation(Email.class), CsActiveForm.MAIN_SCENARIO)));
	}
	
	@Test
	public void getValidatorsOfAnScenario(TestContext context) throws NoSuchFieldException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Form form = new Form();
		Method m = CsActiveForm.class.getDeclaredMethod("getValidators", Field.class, String.class);
		m.setAccessible(true);
		Field f = Form.class.getField("validationInScenario");
		@SuppressWarnings("unchecked")
		List<CsValidator> validators = (List<CsValidator>) m.invoke(form, f, "testScenario");
		
		context.assertTrue(validators.contains(CsValidator.create(RequiredValidation.class, f.getAnnotation(Required.class), "testScenario")));
	}
	
	@Test
	public void validatorsNotInScenarioNotGet(TestContext context) throws NoSuchFieldException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Form form = new Form();
		Method m = CsActiveForm.class.getDeclaredMethod("getValidators", Field.class, String.class);
		m.setAccessible(true);
		Field f = Form.class.getField("validationInScenario");
		@SuppressWarnings("unchecked")
		List<CsValidator> validators = (List<CsValidator>) m.invoke(form, f, CsActiveForm.MAIN_SCENARIO);
		
		context.assertFalse(validators.contains(CsValidator.create(RequiredValidation.class, f.getAnnotation(Required.class), CsActiveForm.MAIN_SCENARIO)));
	}
	
	@Test
	public void validatorWithMultiplesScenariosAreGetInMultipleScenarios(TestContext context) throws NoSuchFieldException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Form form = new Form();
		Method m = CsActiveForm.class.getDeclaredMethod("getValidators", Field.class, String.class);
		m.setAccessible(true);
		Field f = Form.class.getField("validationMultipleScenarios");
		@SuppressWarnings("unchecked")
		List<CsValidator> validators1 = (List<CsValidator>) m.invoke(form, f, CsActiveForm.MAIN_SCENARIO);
		@SuppressWarnings("unchecked")
		List<CsValidator> validators2 = (List<CsValidator>) m.invoke(form, f, "testScenario");
		
		context.assertTrue(validators1.contains(CsValidator.create(RequiredValidation.class, f.getAnnotation(Required.class), CsActiveForm.MAIN_SCENARIO)));
		context.assertTrue(validators2.contains(CsValidator.create(RequiredValidation.class, f.getAnnotation(Required.class), "testScenario")));
	}
	
	@Test
	public void getValidatorsOfTheType(TestContext context) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Form form = new Form();
		Method m = CsActiveForm.class.getDeclaredMethod("getValidators", String.class);
		m.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<CsValidator> validators = (List<CsValidator>) m.invoke(form, CsActiveForm.MAIN_SCENARIO);
		
		context.assertTrue(validators.contains(CsValidator.create(TestTypeValidatorValidation.class, form.getClass().getAnnotation(TestTypeValidator.class), CsActiveForm.MAIN_SCENARIO)));
	}
	
	// TEST validate
	
	@Test
	public void returnsFalseWhenConstraintOfTypeIsInvalid(TestContext context) {
		FormWithTypeValidationError form = new FormWithTypeValidationError();
		
		form.validate(res -> {
			context.assertFalse(res.result());
		});
	}
	
	@Test
	public void returnsTrueWhenConstraintOfTypeIsValid(TestContext context) {
		Form form = new Form();
		doValidatedAttributeValid(form);
		doValidationMultipleScenariosValid(form);
		
		form.validate(res -> {
			if(res.failed())
				res.cause().printStackTrace();
			context.assertTrue(res.result());
		});
	}
	
	@Test
	public void returnsFalseOnAllAttributesError(TestContext context) {
		Form form = new Form();
		
		form.validate(res -> {
			context.assertFalse(res.result());
		});
	}
	
	@Test
	public void returnsFalseOnOneAttributeError(TestContext context) {
		Form form = new Form();
		doValidationMultipleScenariosValid(form);
		
		form.validate(res -> {
			context.assertFalse(res.result());
		});
	}
	
	@Test
	public void returnsTrueOnNoAttributesError(TestContext context) {
		Form form = new Form();
		doValidatedAttributeValid(form);
		doValidationMultipleScenariosValid(form);
		
		form.validate(res -> {
			context.assertTrue(res.result());
		});
	}
	
	@Test
	public void errorsAreClearedOnEachValidation(TestContext context) {
		Form form = new Form();
		
		doValidatedAttributeValid(form);
		form.validate(res -> {
			context.assertFalse(res.result());
		});
		
		doValidationMultipleScenariosValid(form);
		form.validate(res -> {
			context.assertTrue(res.result());
		});
	}
	
	@Test
	public void eventsAreTrigedOnValidate(TestContext context) {
		Form form = new Form();
		doValidatedAttributeValid(form);
		doValidationMultipleScenariosValid(form);
		form.validate(res -> {
			context.assertTrue(beforeValidateCalled);
			context.assertTrue(afterValidateCalled);
		});
	}
	
	
	@Test
	public void runsOnlyValidatorsOfTheScenarioAndGenerals(TestContext context) {
		Form form = new Form();
		form.setScenario("testScenario");
		
		form.validate(res -> {
			context.assertFalse(res.result());
		});
		
		doValidatedAttributeValid(form);
		doValidationInScenarioValid(form);
		doValidationMultipleScenariosValid(form);
		form.validate(res -> {
			context.assertTrue(res.result());
		});
	}
	
	// TEST setAttributes
	
	@Test
	public void setCorrectlyAJsonObjectBody(TestContext context) {
		Form form = new Form();
		
		JsonObject body = new JsonObject();
		body.put("attribute1", "notSet");
		body.put("safeAttribute", "set");
		body.put("validatedAttribute", "set");
		body.put("validationInScenario", "notSet");
		body.put("validationMultipleScenarios", "set");
		form.setAttributes(body);
		
		context.assertEquals(form.safeAttribute, "set");
		context.assertEquals(form.validatedAttribute, "set");
		context.assertEquals(form.validationMultipleScenarios, "set");
		
		context.assertNull(form.attribute1);
		context.assertNull(form.validationInScenario);
	}
	
	@Test
	public void setCorrectlyAJsonObjectBodyInACustomScenario(TestContext context) {
		Form form = new Form();
		form.setScenario("testScenario");
		
		JsonObject body = new JsonObject();
		body.put("attribute1", "set");
		body.put("safeAttribute", "set");
		body.put("validatedAttribute", "set");
		body.put("validationInScenario", "set");
		body.put("validationMultipleScenarios", "set");
		form.setAttributes(body);
		
		context.assertEquals(form.validatedAttribute, "set");
		context.assertEquals(form.safeAttribute, "set");
		context.assertEquals(form.validationMultipleScenarios, "set");
		context.assertEquals(form.validationInScenario, "set");
		
		context.assertNull(form.attribute1);
	}
	
	// TEST setAttributes
	
	@Test
	public void getTheCorrectValuesForAllTheAttributes(TestContext context) throws JsonProcessingException {
		Form form = new Form();
		form.attribute1 = "value1";
		form.safeAttribute = "value2";
		
		JsonObject result = form.getAttributes();
		
		context.assertEquals(result.getString("attribute1"), "value1");
		context.assertEquals(result.getString("safeAttribute"), "value2");
	}
	
	@SuppressWarnings("serial")
	@Test
	public void getTheCorrectValuesForAListOfAttributes(TestContext context) throws JsonProcessingException {
		Form form = new Form();
		form.attribute1 = "value1";
		form.safeAttribute = "value2";
		
		JsonObject result = form.getAttributes(new ArrayList<String>(){{add("attribute1");}});
		
		context.assertEquals(result.getString("attribute1"), "value1");
		context.assertNull(result.getString("safeAttribute"));
	}
	
	
	// TEST HOOKS HANDLERS
	
	@Test
	public void validateReturnFalseWhenOnBeforeValidateReturnFalse() throws CsClientNotInitializedException {
		Form form = new Form();
		doValidatedAttributeValid(form);
		doValidationMultipleScenariosValid(form);
		beforeValidateResult = false;
		
		form.validate(res -> {
			assertFalse(res.result());
			assertFalse(afterValidateCalled);
			assertTrue(beforeValidateCalled);
		});
	}
	
	@Test
	public void validateReturnFalseWhenOnAftervalidateReturnFalse() throws CsClientNotInitializedException {
		Form form = new Form();
		doValidatedAttributeValid(form);
		doValidationMultipleScenariosValid(form);
		afterValidateResult = false;
		
		form.validate(res -> {
			assertFalse(res.result());
			assertTrue(afterValidateCalled);
			assertTrue(beforeValidateCalled);
		});
	}
	
	
	
	// HELPERS
	
	private void callEventMethod(String name, String hookWhere, CsHookHandler<Form> handler, Handler<AsyncResult<Boolean>> resultHandler) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Form form = new Form();
		
		if(hookWhere != null) {
			form.registerHook(hookWhere, handler);
		}
		
		Method m = setMethodAccesible(name);
		m.invoke(form, resultHandler);
	}
	
	private void callEventMethod(String name, Handler<AsyncResult<Boolean>> resultHandler) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod(name, null, null, resultHandler);
	}
	
	private Method setMethodAccesible(String methodName) throws NoSuchMethodException, SecurityException {
		Method m = CsActiveForm.class.getDeclaredMethod(methodName, Handler.class);
		m.setAccessible(true);
		
		return m;
	}
	
	public void doValidatedAttributeValid(Form form) {
		form.validatedAttribute = "test@email.com";
	}
	
	public void doValidationInScenarioValid(Form form) {
		form.validationInScenario = "test";
	}
	
	public void doValidationMultipleScenariosValid(Form form) {
		form.validationMultipleScenarios = "test";
	}
	
	
	// CLASSES HELPERS
	
	@TestTypeValidator
	public class Form extends CsActiveForm<Form> {
		
		public String attribute1;
		
		@Safe
		public String safeAttribute;
		
		@Required
		@Email
		public String validatedAttribute;
		
		@Required(scenarios={"testScenario"})
		public String validationInScenario;
		
		@Required(scenarios={CsActiveForm.MAIN_SCENARIO, "testScenario"})
		public String validationMultipleScenarios;
		
		@SuppressWarnings("unused")
		private String notAttribute;
		
		@BeforeValidate
		public void beforeValidate(Handler<AsyncResult<Boolean>> handler) {
			beforeValidateCalled = true;
			handler.handle(Future.succeededFuture(beforeValidateResult));
		}
		
		@AfterValidate
		public void afterValidate(Handler<AsyncResult<Boolean>> handler) {
			afterValidateCalled = true;
			handler.handle(Future.succeededFuture(afterValidateResult));
		}
	}

	@TestTypeValidator(hasError=true)
	public class FormWithTypeValidationError extends CsActiveForm<FormWithTypeValidationError> {
		
	}
}
