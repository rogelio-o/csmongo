package com.cloudsiness.csmongo.factory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;

import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CsMongoFactoryTest {
	
	public static final String CONNECTION_STRING = "mongodb://localhost:27017";
	public static final String DB_NAME = "test";
	public static final String POOL_NAME = "pool1";
	
	@Rule
	public RunTestOnContext rule = new RunTestOnContext();
	
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		// Clean factory
		Map<String, MongoClient> clients = new HashMap<String, MongoClient>();
		Field f = CsMongoFactory.class.getDeclaredField("clients");
		f.setAccessible(true);
		f.set(null, clients);
	}

	@Test
	public void retrieveAnInitializedClient(TestContext context) throws CsClientNotInitializedException {
		String clientKey = "TEST1";
		
		CsMongoFactory.init(clientKey, rule.vertx(), CONNECTION_STRING, DB_NAME, false, null, null, null);
		
		context.assertNotNull(CsMongoFactory.mongo(clientKey));
	}
	
	@Test(expected=CsClientNotInitializedException.class)
	public void throwsExceptionWhenTryToRetrieveANonInitializedClient(TestContext context) throws CsClientNotInitializedException {
		String clientKey = "TEST2";
		
		context.assertNull(CsMongoFactory.mongo(clientKey));
	}
	
	@Test
	public void retrieveAnInitializedClientWithPool(TestContext context) throws CsClientNotInitializedException {
		String clientKey = "TEST3";
		
		CsMongoFactory.init(clientKey, rule.vertx(), POOL_NAME, CONNECTION_STRING, DB_NAME, false, null, null, null);
		
		context.assertNotNull(CsMongoFactory.mongo(clientKey));
	}
	
	@Test
	public void retrieveTheMainInitializedClient(TestContext context) throws CsClientNotInitializedException {
		CsMongoFactory.init(rule.vertx(), CONNECTION_STRING, DB_NAME, false, null, null, null);
		
		context.assertNotNull(CsMongoFactory.mongo());
	}

	@Test(expected=CsClientNotInitializedException.class)
	public void throwsExceptionWhenTryToRetrieveTheMainNotInitializedClient(TestContext context) throws CsClientNotInitializedException {
		context.assertNull(CsMongoFactory.mongo());
	}
	
}
