package com.cloudsiness.csmongo.factory;

import java.util.HashMap;
import java.util.Map;

import com.cloudsiness.csmongo.factory.exceptions.CsClientNotInitializedException;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Initializes and saves the mongo clients. Allow the access to all the mongo clients from anywhere statically.
 * 
 * @author 	Rogelio R. Orts Cansino
 * @version 0.1
 */
public class CsMongoFactory {
	
	/**
	 * The key to retrieve the main mongo client.
	 */
	public static final String MAIN = "main";
	
	/**
	 * Saves the initialized mongo clients.
	 */
	private static Map<String, MongoClient> clients = new HashMap<String, MongoClient>();
	
	/**
	 * Initializes a mongo client with the configuration in the parameters and saves it in the key listed in the parameters.
	 * 
	 * @param key					The key where save the client.
	 * @param vertx					The running vertex.
	 * @param pool					The name of the pool with which the client will be shared.
	 * @param connectionString		The string to connect to the MongoDB. For example: mongodb://localhost:27017
	 * @param dbName				The name of the database to which connect to.
	 * @param useObjectId			Toggle this option to support persisting and retrieving ObjectId’s as strings.
	 */
	public static void init(String key, Vertx vertx, String pool, String connectionString, String dbName, boolean keepAlive, String username, String password, String authSource) {
		MongoClient client;
		
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.put("connection_string", connectionString);
		mongoConfig.put("db_name", dbName);
		mongoConfig.put("useObjectId", true);
		mongoConfig.put("keepAlive", keepAlive);
		if(username != null)
			mongoConfig.put("username", username);
		if(password != null)
			mongoConfig.put("password", password);
		if(authSource != null)
			mongoConfig.put("authSource", authSource);
		if(pool != null)
			client = MongoClient.createShared(vertx, mongoConfig, pool);
		else
			client = MongoClient.createNonShared(vertx, mongoConfig);
		
		clients.put(key, client);
	}
	
	/**
	 * Initializes a mongo client with a non shared data pool.
	 * 
	 * @see #init(String, Vertx, String, String, String, boolean)
	 */
	public static void init(String key, Vertx vertx, String connectionString, String dbName, boolean useObjectId, String username, String password, String authSource) {
		init(key, vertx, null, connectionString, dbName, useObjectId, username, password, authSource);
	}
	
	/**
	 * Initializes the main mongo client (saves it with the key for main mongo client) in a shared
	 * data pool.
	 * 
	 * @see #init(String, Vertx, String, String, String, boolean)
	 */
	public static void init(Vertx vertx, String pool, String connectionString, String dbName, boolean useObjectId, String username, String password, String authSource) {
		init(MAIN, vertx, pool, connectionString, dbName, useObjectId, username, password, authSource);
	}
	
	/**
	 * Initializes the main mongo client (saves it with the key for main mongo client) in a non shared
	 * data pool.
	 * 
	 * @see #init(String, Vertx, String, String, String, boolean)
	 */
	public static void init(Vertx vertx, String connectionString, String dbName, boolean useObjectId, String username, String password, String authSource) {
		init(MAIN, vertx, null, connectionString, dbName, useObjectId, username, password, authSource);
	}
	
	/**
	 * Retrieve an instantiated mongo client from the key listed in the parameters.
	 * 
	 * @param key								The key to retrieve the mongo client.
	 * @return									A previously instantiated mongo client.
	 * @throws CsClientNotInitializedException	If the mongo client with this key has not been instantiated previously.
	 */
	public static MongoClient mongo(String key) throws CsClientNotInitializedException {
		MongoClient client = clients.get(key);
		if(client == null)
			throw new CsClientNotInitializedException("The mongo client '" + key + "' is not initialized yet.");
		
		return client;
	}
	
	/**
	 * Retrieves the main mongo client (gets it with the key for main mongo client).
	 * 
	 * @see #mongo(String)
	 */
	public static MongoClient mongo() throws CsClientNotInitializedException {
		return mongo(MAIN);
	}
	
}
