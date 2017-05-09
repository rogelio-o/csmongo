package com.cloudsiness.csmongo.active;

import static org.junit.Assert.*;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.active.annotations.CsModel;
import com.cloudsiness.csmongo.active.exceptions.CsCanNotSaveAttributesOfNewRecords;
import com.cloudsiness.csmongo.active.exceptions.CsModelNotAnnotatedException;
import com.cloudsiness.csmongo.active.helpers.TestModel;
import com.cloudsiness.csmongo.active.hooks.CsHookHandler;
import com.cloudsiness.csmongo.factory.CsMongoFactory;
import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CsMongoFactory.class)
public class CsActiveRecordTest {
	
	// CONSTANTS
	
	public final static String COLLECTION_NAME = "test";
	public final static String MONGO_CLIENT = "test_mongo_client";
	
	@SuppressWarnings("serial")
	public final static List<JsonObject> FIND_ALL_MONGO_RESULT = new LinkedList<JsonObject>() {{
		add(new JsonObject().put("attribute1", "value1_1").put("safeAttribute", "value1_2"));
		add(new JsonObject().put("attribute1", "value2_1").put("safeAttribute", "value2_2"));
	}};
	public final static JsonObject FIND_ONE_MONGO_RESULT = new JsonObject().put("attribute1", "value1").put("safeAttribute", "value2");
	public final static Long COUNT_MONGO_RESULT = 5L;
	public final static String SAVE_MONGO_RESULT = "56928c11dd59030532407aed";
	
	
	// FIXES
	
	private String mongoClientKey;
	
	private boolean hookBeforeSaveCalled;
	private boolean hookAfterSaveCalled;
	private boolean hookBeforeFindCalled;
	private boolean hookAfterFindCalled;
	private boolean hookBeforeCountCalled;
	private boolean hookAfterCountCalled;
	private boolean hookBeforeDeleteCalled;
	private boolean hookAfterDeleteCalled;
	
	private JsonObject savedModel;
	private boolean mongoRemoveIsCalled;
	
	
	// SETUP
	
	@Before
	public void setUp() {
		mongoClientKey = null;
		
		hookBeforeSaveCalled = false;
		hookAfterSaveCalled = false;
		hookBeforeFindCalled = false;
		hookAfterFindCalled = false;
		hookBeforeCountCalled = false;
		hookAfterCountCalled = false;
		hookBeforeDeleteCalled = false;
		hookAfterDeleteCalled = false;
		
		savedModel = null;
		mongoRemoveIsCalled = false;
	}
	
	
	// TESTS - modelType
	
	@Test
	public void theInheritModelClassIsCorrect() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		TestModel model = new TestModel();
		Field f = CsActiveRecord.class.getDeclaredField("modelType");
		f.setAccessible(true);
		
		assertEquals(f.get(model), TestModel.class);
	}
	
	
	// TESTS - getModelAnnotation
	
	@Test(expected=CsModelNotAnnotatedException.class)
	public void throwExceptionWhenTryToGetTheAnnotationOfNotAnnotedModel() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		NotAnnotatedModel model = new NotAnnotatedModel();
		Method m = setMethodAccesible("getModelAnnotation");
		
		assertNull(m.invoke(model));
	}

	
	// TESTS - getCollectionName
	
	@Test
	public void getCollectionNameOfModel() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ModelWithCustomClient model = new ModelWithCustomClient();
		Method m = setMethodAccesible("getCollectionName");
		
		assertEquals(m.invoke(model), COLLECTION_NAME);
	}
	
	
	// TESTS - getMongoClient
	
	@Test
	public void getMongoClientWhenNotSpecifyTheClientKey() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		Method m = setMethodAccesible("getMongoClient");
		
		assertNotNull(m.invoke(model));
		assertEquals(mongoClientKey, CsMongoFactory.MAIN);
	}
	
	@Test
	public void getMongoClientWhenSpecifyTheClientKey() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CsClientNotInitializedException {
		mockMongoClientFactory();
		
		ModelWithCustomClient model = new ModelWithCustomClient();
		Method m = setMethodAccesible("getMongoClient");
		
		assertNotNull(m.invoke(model));
		assertEquals(mongoClientKey, MONGO_CLIENT);
	}
	
	
	// TESTS - callOnBeforeSave
	
	@Test
	public void methodsAreInvokedWhenCallOnBeforeSave() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnBeforeSave", res -> {
			assertTrue(model.isBeforeSaveCalled());
			assertTrue(model.isBeforeSaveCalled2());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnBeforeSave() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnBeforeSave", TestModel.ON_BEFORE_SAVE, (model, res) -> {
			hookBeforeSaveCalled = true;
		}, res -> {
			assertTrue(hookBeforeSaveCalled);
		});
	}

	
	// TESTS - callOnAfterSave
	
	@Test
	public void methodsAreInvokedWhenCallOnAfterSave() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnAfterSave", res -> {
			assertTrue(model.isAfterSaveCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnAfterSave() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnAfterSave", TestModel.ON_AFTER_SAVE, (model, res) -> {
			hookAfterSaveCalled = true;
		}, res -> {
			assertTrue(hookAfterSaveCalled);
		});
	}
	
	
	// TESTS - callOnBeforeFind
	
	@Test
	public void methodsAreInvokedWhenCallOnBeforeFind() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnBeforeFind", res -> {
			assertTrue(model.isBeforeFindCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnBeforeFind() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnBeforeFind", TestModel.ON_BEFORE_FIND, (model, res) -> {
			hookBeforeFindCalled = true;
		}, res -> {
			assertTrue(hookBeforeFindCalled);
		});
	}
	
	
	// TESTS - callOnAfterFind
	
	@Test
	public void methodsAreInvokedWhenCallOnAfterFind() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnAfterFind", res -> {
			assertTrue(model.isAfterFindCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnAfterFind() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnAfterFind", TestModel.ON_AFTER_FIND, (model, res) -> {
			hookAfterFindCalled = true;
		}, res -> {
			assertTrue(hookAfterFindCalled);
		});
	}
	
	
	// TESTS - callOnBeforeCount
	
	@Test
	public void methodsAreInvokedWhenCallOnBeforeCount() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnBeforeCount", res -> {
			assertTrue(model.isBeforeCountCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnBeforeCount() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnBeforeCount", TestModel.ON_BEFORE_COUNT, (model, res) -> {
			hookBeforeCountCalled = true;
		}, res -> {
			assertTrue(hookBeforeCountCalled);
		});
	}
	
	
	// TESTS - callOnAfterCount
	
	@Test
	public void methodsAreInvokedWhenCallOnAfterCount() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnAfterCount", res -> {
			assertTrue(model.isAfterCountCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnAfterCount() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnAfterCount", TestModel.ON_AFTER_COUNT, (model, res) -> {
			hookAfterCountCalled = true;
		}, res -> {
			assertTrue(hookAfterCountCalled);
		});
	}
	
	
	// TESTS - callOnBeforeDelete
	
	@Test
	public void methodsAreInvokedWhenCallOnBeforeDelete() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnBeforeDelete", res -> {
			assertTrue(model.isBeforeDeleteCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnBeforeDelete() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnBeforeDelete", TestModel.ON_BEFORE_DELETE, (model, res) -> {
			hookBeforeDeleteCalled = true;
		}, res -> {
			assertTrue(hookBeforeDeleteCalled);
		});
	}
	
	// TESTS - callOnAfterCount
	
	@Test
	public void methodsAreInvokedWhenCallOnAfterDelete() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		
		callEventMethod(model, "callOnAfterDelete", res -> {
			assertTrue(model.isAfterDeleteCalled());
		});
	}
	
	@Test
	public void hooksAreCalledWhenCallOnAfterDelete() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callEventMethod("callOnAfterDelete", TestModel.ON_AFTER_DELETE, (model, res) -> {
			hookAfterDeleteCalled = true;
		}, res -> {
			assertTrue(hookAfterDeleteCalled);
		});
	}
	
	
	// TEST - getAttributes
	
	@Test
	public void allAttributesGet() throws NoSuchFieldException, SecurityException {
		TestModel form = new TestModel();
		List<Field> attributes = form.getFields();
		
		assertTrue(attributes.contains(TestModel.class.getDeclaredField("attribute1")));
		assertTrue(attributes.contains(TestModel.class.getDeclaredField("safeAttribute")));
	}
	
	
	// TEST - initializeModel
	
	@Test
	public void theAttributesAreSetWithTheRightValuesWhenInitializeModel() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		invokeInitializeModel(new JsonObject().put("attribute1", "value1").put("safeAttribute", "value2"), res -> {
			TestModel model = res.result();
			
			assertEquals(model.attribute1, "value1");
			assertEquals(model.safeAttribute, "value2");
		});
	}
	
	@Test
	public void eventsAreTriggedSuccessfullyWhenInitializeModel() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		invokeInitializeModel(new JsonObject(), res -> {
			TestModel model = res.result();
			
			assertTrue(model.isBeforeFindCalled());
			assertTrue(model.isAfterFindCalled());
		});
		
		
	}
	
	@Test
	public void initializedModelsAreNotNewRecords() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		invokeInitializeModel(new JsonObject(), res -> {
			assertFalse(res.result().isNewRecord());
		});
	}
	
	
	// TEST - initializeModels
	
	@Test
	public void allTheModelsAreRightInitialized() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model1 = new TestModel();
		model1.attribute1 = "object1_value1";
		model1.safeAttribute = "object1_value2";
		TestModel model2 = new TestModel();
		model2.attribute1 = "object2_value1";
		model2.safeAttribute = "object2_value2";
		List<JsonObject> objects = new LinkedList<JsonObject>();
		objects.add(new JsonObject().put("attribute1", model1.attribute1).put("safeAttribute", model1.safeAttribute));
		objects.add(new JsonObject().put("attribute1", model2.attribute1).put("safeAttribute", model2.safeAttribute));
		
		Handler<AsyncResult<List<TestModel>>> handler = res -> {
			List<TestModel> result = res.result();
			
			assertTrue(result.contains(model1));
			assertTrue(result.contains(model2));
		};
		
		Method m = setMethodAccesible("initializeModels", List.class, Handler.class);
		m.invoke(model1, objects, handler);
	}
	
	
	// TEST - findAll
	
	@Test 
	public void getsTheRightListOfModelWhenFindAll() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model.findAll(res -> {
			assertEquals(res.result(), getFindAllResult());
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenFindAll() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.findAll(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenFindAll() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.findAll(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void allTheFindEventsAreTriggedInEachModelWhenFindAll() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model.findAll(res -> {
			for(TestModel m : res.result()) {
				assertTrue(m.isBeforeFindCalled());
				assertTrue(m.isAfterFindCalled());
			}
		});
	}
	
	
	// TEST - find
	
	@Test
	public void getsTheModelWhenFind() throws CsClientNotInitializedException {		
		mockMongoClientFactory();
		
		new TestModel().find(new JsonObject(), res -> {
			assertEquals(res.result(), getFindResult());
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenFind() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.find(new JsonObject(), res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenFind() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.find(new JsonObject(), res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void allTheFindEventsAreTriggedInTheModelWhenFind() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model.find(new JsonObject(), res -> {
			assertTrue(res.result().isBeforeFindCalled());
			assertTrue(res.result().isAfterFindCalled());
		});
	}
	
	
	// TEST - count
	
	@Test
	public void getTheRightValueWhenCount() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		new TestModel().count(res -> {
			assertEquals(res.result(), COUNT_MONGO_RESULT);
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenCount() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.count(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenCount() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.count(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void allTheCountEventsAreTriggedInTheModelWhenCount() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model.count(res -> {
			assertTrue(model.isBeforeCountCalled());
			assertTrue(model.isAfterCountCalled());
		});
	}
	
	
	// TEST - exists
	
	@Test
	public void getTheRightValueWhenExists() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		new TestModel().exists(res -> {
			assertEquals(res.result(), COUNT_MONGO_RESULT > 0);
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenExists() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.exists(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenExists() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.exists(res -> {
			assertTrue(res.failed());
		});
	}
	
	
	// TEST - save
	
	@Test
	public void modelIdAreSetFromTheMongoResult() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = validForSaveModel();
		
		model.save(res -> {
			assertEquals(model._id.toString(), SAVE_MONGO_RESULT);
		});
	}
	
	@Test
	public void modelIsNotANewRecordAfterSave() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = validForSaveModel();
		
		assertTrue(model.isNewRecord());
		model.save(res -> {
			assertFalse(model.isNewRecord());
		});
	}
	
	@Test
	public void falseFutureIfIsNotValidModelWhenSave() {
		TestModel model = new TestModel();
		
		model.save(res -> {
			assertFalse(res.result());
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenSave() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.count(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenSave() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.safeAttribute = "test";
		model.save(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void allTheSaveEventsAreTriggedInTheModelWhenSave() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = validForSaveModel();
		
		model.save(res -> {
			assertTrue(model.isBeforeSaveCalled());
			assertTrue(model.isAfterSaveCalled());
		});
	}
	
	@Test
	public void ignoredAttributesAreIgnoredOnSave() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = validForSaveModel();
		model.ignoredAttribute = "ignored";
		
		model.save(res -> {
			assertNull(savedModel.getString("ignoredAttribute"));
		});
	}
	
	
	// TEST - saveAttributes
	
	@SuppressWarnings("serial")
	@Test
	public void rightAttributesAreSentToTheMongoClientWhenSaveAttributes() throws CsClientNotInitializedException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		mockMongoClientFactory();
		TestModel model = validForSaveModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		setModelAsNotNewRecord(model);
		List<String> attributes = new ArrayList<String>() {{ add("attribute1"); }};
		
		model.saveAttributes(attributes, res -> {
			assertTrue(res.succeeded());
			JsonObject update = savedModel.getJsonObject("$set");
			assertNull(update.getString("safeAttribute"));
			assertEquals(update.getString("attribute1"), model.attribute1);
		});
	}
	
	@Test
	public void newRecordsCanNotBeSavedWhenSaveAttributes() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = new TestModel();

		model.saveAttributes(new ArrayList<String>(), res -> {
			assertEquals(res.cause().getClass(), CsCanNotSaveAttributesOfNewRecords.class);
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenSaveAttributes() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.saveAttributes(new ArrayList<String>(), res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenSaveAttributes() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.saveAttributes(new ArrayList<String>(), res -> {
			assertTrue(res.failed());
		});
	}
	
	
	// TEST - delete
	
	@Test
	public void checkMongoRemoveIsCalledWhenDelete() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = new TestModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		
		model.delete(res -> {
			assertTrue(res.succeeded());
			assertTrue(mongoRemoveIsCalled);
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenDelete() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		model.delete(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenDelete() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		model.delete(res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void allTheSaveEventsAreTriggedInTheModelWhenDelete() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = validForSaveModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		
		model.delete(res -> {
			assertTrue(model.isBeforeDeleteCalled());
			assertTrue(model.isAfterDeleteCalled());
		});
	}
	
	
	// TEST - deleteAll
	
	@Test
	public void checkMongoRemoveIsCalledWhenDeleteAll() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		TestModel model = new TestModel();
		
		model.deleteAll(new JsonObject(), res -> {
			assertTrue(res.succeeded());
			assertTrue(mongoRemoveIsCalled);
		});
	}
	
	@Test
	public void failedFutureOnMongoClientExceptionWhenDeleteAll() throws CsClientNotInitializedException {
		mockMongoClientFactoryWithException();
		
		TestModel model = new TestModel();
		model.deleteAll(new JsonObject(), res -> {
			assertTrue(res.failed());
		});
	}
	
	@Test
	public void failedFutureOnFailedMongoResponseWhenDeleteAll() throws CsClientNotInitializedException {
		mockMongoClientWithFailFuture();
		
		TestModel model = new TestModel();
		model.deleteAll(new JsonObject(), res -> {
			assertTrue(res.failed());
		});
	}
	
	
	// TEST HOOKS HANDLERS
	
	@Test
	public void saveReturnFalseWhenOnBeforeSaveReturnFalse() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model.safeAttribute = "test";
		model.setBeforeSaveResult(false);
		
		model.save(res -> {
			assertFalse(res.result());
			assertFalse(model.isAfterSaveCalled());
			assertTrue(model.isBeforeSaveCalled());
		});
	}
	
	@Test
	public void saveReturnFalseWhenOnAfterSaveReturnFalse() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model.safeAttribute = "test";
		model.setAfterSaveResult(false);
		
		model.save(res -> {
			assertFalse(res.result());
			assertTrue(model.isAfterSaveCalled());
			assertTrue(model.isBeforeSaveCalled());
		});
	}
	
	@Test
	public void deleteReturnFalseWhenOnBeforeDeleteReturnFalse() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		model.setBeforeDeleteResult(false);
		
		model.delete(res -> {
			assertFalse(res.result());
			assertFalse(model.isAfterDeleteCalled());
			assertTrue(model.isBeforeDeleteCalled());
		});
	}
	
	@Test
	public void deleteReturnFalseWhenOnAfterDeleteReturnFalse() throws CsClientNotInitializedException {
		mockMongoClientFactory();
		
		TestModel model = new TestModel();
		model._id = new ObjectId("56acba086c5ebc0532e9622b");
		model.setAfterDeleteResult(false);
		
		model.delete(res -> {
			assertFalse(res.result());
			assertTrue(model.isAfterDeleteCalled());
			assertTrue(model.isBeforeDeleteCalled());
		});
	}
	
	
	// HELPERS
	
	private TestModel callEventMethod(TestModel model, String name, String hookWhere, CsHookHandler<TestModel> handler, Handler<AsyncResult<Boolean>> resultHandler) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(model == null)
			model = new TestModel();
		
		if(hookWhere != null) {
			model.registerHook(hookWhere, handler);
		}
		
		Method m = setMethodAccesible(name, Handler.class);
		m.invoke(model, resultHandler);
		
		return model;
	}
	
	private TestModel callEventMethod(String name, String hookWhere, CsHookHandler<TestModel> handler, Handler<AsyncResult<Boolean>> resultHandler) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return callEventMethod(null, name, hookWhere, handler, resultHandler);
	}
	
	private TestModel callEventMethod(TestModel model, String name, Handler<AsyncResult<Boolean>> resultHandler) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return callEventMethod(model, name, null, null, resultHandler);
	}
	
	private Method setMethodAccesible(String methodName, Class<?>... params) throws NoSuchMethodException, SecurityException {
		Method m;
		try {
			m = CsActiveRecord.class.getDeclaredMethod(methodName, params);
		} catch(NoSuchMethodException e) {
			m = CsActiveBase.class.getDeclaredMethod(methodName, params);
		}
		m.setAccessible(true);
		
		return m;
	}
	
	private void mockMongoClientFactory(MongoClient mongoClient) throws CsClientNotInitializedException {
		mockStatic(CsMongoFactory.class);
		MongoClient client;
		
		if(mongoClient == null)
			client = mock(MongoClient.class);
		else
			client = mongoClient;
		
		when(CsMongoFactory.mongo()).thenAnswer(invocation -> {
			return client;
		});
		
		when(CsMongoFactory.mongo(anyString())).thenAnswer(invocation -> {
			mongoClientKey = (String) invocation.getArguments()[0];
			
			return client;
		});
	}
	
	private void mockMongoClientFactory() throws CsClientNotInitializedException {
		MongoClient client = mock(MongoClient.class);
		when(client.find(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<List<JsonObject>>> h = inv.getArgumentAt(2, Handler.class);
			List<JsonObject> result = new LinkedList<JsonObject>();
			
			for(JsonObject model : FIND_ALL_MONGO_RESULT) {
				model.put("query", inv.getArgumentAt(1, Handler.class));
				result.add(model);
			}
			h.handle(Future.succeededFuture(result));
			
			return null;
		});
		when(client.findOne(any(), any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<JsonObject>> h = inv.getArgumentAt(3, Handler.class);
			JsonObject result = FIND_ONE_MONGO_RESULT;
			result.put("query", inv.getArgumentAt(1, Handler.class));
			h.handle(Future.succeededFuture(result));
			
			return null;
		});
		when(client.count(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Long>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.succeededFuture(COUNT_MONGO_RESULT));
			
			return null;
		});
		when(client.save(any(), any(), any())).then(inv -> {
			savedModel = (JsonObject) inv.getArgumentAt(1, JsonObject.class);
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<String>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.succeededFuture(SAVE_MONGO_RESULT));
			
			return null;
		});
		when(client.updateCollection(any(), any(), any(), any())).then(inv -> {
			savedModel = (JsonObject) inv.getArgumentAt(2, JsonObject.class);
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Void>> h = inv.getArgumentAt(3, Handler.class);
			h.handle(Future.succeededFuture());
			
			return null;
		});
		when(client.removeDocument(any(), any(), any())).then(inv -> {
			mongoRemoveIsCalled = true;
			
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Void>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.succeededFuture());
			
			return null;
		});
		when(client.removeDocuments(any(), any(), any())).then(inv -> {
			mongoRemoveIsCalled = true;
			
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Void>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.succeededFuture());
			
			return null;
		});
		
		
		mockMongoClientFactory(client);
	}
	
	private List<TestModel> getFindAllResult() {
		List<TestModel> result = new LinkedList<TestModel>();
		
		for(JsonObject obj : FIND_ALL_MONGO_RESULT) {
			result.add(new TestModel(obj.getString("attribute1"), obj.getString("safeAttribute")));
		}
		
		return result;
	}
	
	private TestModel getFindResult() {
		return new TestModel(FIND_ONE_MONGO_RESULT.getString("attribute1"), FIND_ONE_MONGO_RESULT.getString("safeAttribute"));
	}
	
	private TestModel validForSaveModel() {
		TestModel model = new TestModel();
		model.attribute1 = "value1";
		model.safeAttribute = "value2";
		
		return model;
	}
	
	private void setModelAsNotNewRecord(CsActiveRecord<?> model) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = CsActiveBase.class.getDeclaredField("isNewRecord");
		f.setAccessible(true);
		f.set(model, false);
	}
	
	private void mockMongoClientWithFailFuture() throws CsClientNotInitializedException {
		MongoClient client = mock(MongoClient.class);
		when(client.find(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<List<JsonObject>>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		when(client.findOne(any(), any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<JsonObject>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		when(client.count(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Long>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		when(client.save(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<String>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		when(client.updateCollection(any(), any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Void>> h = inv.getArgumentAt(3, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		when(client.removeDocument(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Void>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		when(client.removeDocuments(any(), any(), any())).then(inv -> {
			@SuppressWarnings("unchecked")
			Handler<AsyncResult<Void>> h = inv.getArgumentAt(2, Handler.class);
			h.handle(Future.failedFuture("test"));
			
			return null;
		});
		
		mockMongoClientFactory(client);
	}
	
	private void mockMongoClientFactoryWithException() throws CsClientNotInitializedException {
		mockStatic(CsMongoFactory.class);
		when(CsMongoFactory.mongo()).thenAnswer(invocation -> {
			throw new CsClientNotInitializedException("test");
		});
	}
	
	private void invokeInitializeModel(JsonObject values, Handler<AsyncResult<TestModel>> handler) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestModel model = new TestModel();
		Method m = setMethodAccesible("initializeModel", JsonObject.class, Handler.class);
		m.invoke(model, values, handler);
	}
	
	
	// CLASSES HELPERS
	
	
	
	@CsModel(collectionName=COLLECTION_NAME, mongoClient=MONGO_CLIENT)
	private class ModelWithCustomClient extends CsActiveRecord<ModelWithCustomClient> {

		public ModelWithCustomClient() {
			super();
		}
		
	}
	
	private class NotAnnotatedModel extends CsActiveRecord<NotAnnotatedModel> {

		public NotAnnotatedModel() {
			super();
		}
		
	}
}
