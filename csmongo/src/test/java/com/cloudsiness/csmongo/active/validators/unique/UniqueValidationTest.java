package com.cloudsiness.csmongo.active.validators.unique;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.active.annotations.CsModel;
import com.cloudsiness.csmongo.factory.CsMongoFactory;
import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.mongo.MongoClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CsMongoFactory.class)
public class UniqueValidationTest {
	
	private UniqueValidation validator;
	
	@Before
	public void setUp() {
		validator = new UniqueValidation();
	}

	
	@Test
	public void returnNullIfIsUniqueValue() throws CsClientNotInitializedException, NoSuchFieldException, SecurityException {
		mockMongoFactory(false);
		initializeValidator("unique");
		
		TestModel model = new TestModel();
		model.unique = "unique";
		
		validator.isValid(model, "unique", res -> {
			assertNull(res.result());
		});
	}
	
	@Test
	public void returnNotNullIfIsNotUniqueValue() throws CsClientNotInitializedException, NoSuchFieldException, SecurityException {
		mockMongoFactory(true);
		initializeValidator("unique");
		
		TestModel model = new TestModel();
		model.unique = "notUnique";
		
		validator.isValid(model, "unique", res -> {
			assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnNotNullIfIsNullAndCanNotBeNull() throws NoSuchFieldException, SecurityException, CsClientNotInitializedException {
		mockMongoFactory(true);
		initializeValidator("unique");
		
		TestModel model = new TestModel();
		model.unique = null;
		
		validator.isValid(model, "unique", res -> {
			assertNotNull(res.result());
		});
	}
	
	@Test
	public void returnNullIfIsNullAndCanBeNull() throws NoSuchFieldException, SecurityException, CsClientNotInitializedException {
		mockMongoFactory(true);
		initializeValidator("uniqueNull");
		
		TestModel model = new TestModel();
		model.uniqueNull = null;
		
		validator.isValid(model, "uniqueNull", res -> {
			assertNull(res.result());
		});
	}
	
	private void initializeValidator(String field) throws NoSuchFieldException, SecurityException {
		validator.initialize(TestModel.class.getField(field).getAnnotation(Unique.class));
	}

	private void mockMongoFactory(boolean returnExists) throws CsClientNotInitializedException {
		mockStatic(CsMongoFactory.class);
		MongoClient client = mongoClient(returnExists);
		when(CsMongoFactory.mongo(any())).thenReturn(client);
	}
	
	@SuppressWarnings("unchecked")
	private MongoClient mongoClient(boolean returnExists) {
		MongoClient result = mock(MongoClient.class);
		
		when(result.count(any(), any(), any())).thenAnswer(invoke -> {
			Handler<AsyncResult<Long>> handler = invoke.getArgumentAt(2, Handler.class);
			handler.handle(Future.succeededFuture(returnExists ? 1L : 0L));
			
			return result;
		});
		
		return result;
	}
	
	@CsModel(collectionName = "test")
	private class TestModel extends CsActiveRecord<TestModel> {

		@Unique
		public String unique;
		
		@Unique(canBeNull=true)
		public String uniqueNull;
		
		public TestModel() {
			super();
		}
		
	}
}
