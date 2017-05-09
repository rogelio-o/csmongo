# CsMongo

## What is this?

CsMongo is an implementation of the [Active Record pattern](https://en.wikipedia.org/wiki/Active_record_pattern) for [Vert.x](http://vertx.io/) and [MongoDB](https://www.mongodb.com/es).


## Getting started

### Initialization

Once you have imported CsMongo into your project, you have to initialize de MongoDB connection.

```
CsMongoFactory.init(Vertx vertx, String connectionString, String dbName, boolean useObjectId = true, String username = null, String password = null, String authSource = null);
```
You can initialize more than one MongoDB connection, so you can work with models in different databases. In this case, you have to add a key to identify the connection. It is important to note that in the Active Record classes with the not main connection you will have to indicate the connection that you want to use (we will see how in Active Record section).
```
CsMongoFactory.init(String key, Vertx vertx, String connectionString, String dbName, boolean useObjectId = true, String username = null, String password = null, String authSource = null);
```
In case you want to use only the Active Form (not the Active Record), you don't have to initialize the MongoDB connection.


### Criteria

The **CsCriteria** class encapsulates the data of a fetch. You can set: the query as a JsonObject or as (field name, value), the fields you want to retrieve, the sort as a JsonObject or as a (field name, [1 for ASC and -1 for DESC]), the query limit and the number of documents to skip in the query.

```
JsonObject query = new JsonObject()
	.put("age", new JsonObject()
		.put("$gt", 18)
	);
CsCriteria criteria = new CsCriteria()
	.setQuery(query)
	.addCondition("city", "Seville")
	.setSort("name", 1)
	.setLimit(10)
	.setSkip(5);

```


### Active Form

The class **CsActiveForm** has features to fill a form from MultiMap params and validate them. To use it, you only have to extend the class **CsActiveForm**.

```
public class MyForm extends CsActiveForm<MyForm> {
	
	@Required
	@Email
	public String email;

	@Required
	public String password;

	@Required
	public String not_a_robot;

	@Safe
	public Boolean remember_me;

}
```

#### Attributes

The Active Form and the Active Record only work with public attributes, the private attributes are not used by them.

If you want to fill a form (or an active record) from a posted form, you can use the setAttributes method with a MultiMap or a JsonObject.
**IMPORTANT:** onle safe attributes are set by setAttributes. **What is a safe attribute?** A safe attribute is a public attribute that is annotated for its validation or it is annotated with _@Safe_. An attribute can be safe in some scenarios and unsafe in some others.

#### Scenarios

There will be validators that will be used only in some cases and attributes that will be safe only in some occasions. In this cases, you can use scenarios. Adding ```scenarios=["SCENARIO 1", "SCENARIO 2"]``` to the annotation, the validator will be applied (or the attribute will be safe) only if the scenario is one of the list.
You can set the scenario using the method setScenario(String scenarioName) and get it using the method getScenario.

```
	@Required(scenarios=["CREATION"])
	@Safe(scenarios=["UPDATE"])
	public String name;

	...

	form.setScenario("CREATION");
	form.validate(...);
```

#### Validation

You can annotate an attribute and it will be validated by the related validator. The available validators are:
- @AssertFalse
- @AssertTrue
- @Compare
	- attribute (String): the other attribute to compare.
- @Digits
	- integer (int): maximum number of integral digits accepted for this number
	- fraction (int): maximum number of fractional digits accepted for this number
- @Email
	- pattern (String): (optional, if there is no pattern it will be compared with the default pattern)
- @Future
- @Past
- @DecimalMax
	- value (String): value the element must be lower or equal to
	- inclusive (boolean): Specifies whether the specified maximum is inclusive or exclusive. (default = true)
- @DecimalMin
	- value (String): value the element must be higher or equal to
	- inclusive (boolean): Specifies whether the specified minimum is inclusive or exclusive. (default = true)
- @Max
	- value (long): value the element must be lower or equal to
	- inclusive (boolean): Specifies whether the specified maximum is inclusive or exclusive. (default = true)
- @Min
	- value (long): value the element must be higher or equal to
	- inclusive (boolean): Specifies whether the specified minimum is inclusive or exclusive. (default = true)
- @Pattern
	- regexp (String): regex to check
	- flags (int[]): (optional)
- @Size
	- min (int): min size
	- max (int): max size
	- allowEmpty (boolean): if the attribute can be null or empty (default = false)
- @Unique
	- canBeNull (boolean): if the attribute can be null (default = false)
	- nullIsAValue (boolean): (default = false)
- @Unique2
	- attribute2 (String)
	- canBeNull (boolean): if the attribute can be null (default = false)
	- nullIsAValue (boolean): (default = false)
- @Unique3
	- attribute2 (String)
	- attribute3 (String)
	- canBeNull (boolean): if the attribute can be null (default = false)
	- nullIsAValue (boolean): (default = false)


If you add the param ```message="MESSAGE"``` to the annotation, you will get that message when there is an error instead of the default message.

To get the errors, you can use the method getErrors or getErrors(String fieldName). The errors will be returned as **CsValidationError** including: the message, the related attributes and the related values. Also, you can check if an active class has errors using the method hasErrors or hasErrors(String fieldName).

**To do the validation, you have to use the method ```validate(Handler<AsyncResult<Boolean>> handler)```.** Each time the validation is done, the previously added errors are cleared.

#### Behaviours

You can add methods that will be executed when an event happens. The method has to be only a parameter of type ```Handler<AsyncResult<Boolean>>``` and it can not return anything. If the result of the handler is _true_, the process will carry on. The method has to be annotated with one ore more of the followings annotations:

- @BeforeValidate
- @AfterValidate

```
@AfterValidate
public void checkSomething(Handler<AsyncResult<Boolean>> handler) {
	if(somethingHappens) {
		handler.handler(Future.succeededFuture(true));
	} else {
		handler.handler(Future.succeededFuture(false));
	}
}
```


### Active Record

The class **CsActiveRecord** extends CsActiveForm and has features to fetch, save, delete,... documents from MongoDB.

#### Creation

To create an active record you have to extend the class CsActiveRecord and annotate the new class with ** @ CsModel **. The params of the annotation will be:
- collectionName: name of the collection for that model.
- mongoClient: (optional) if you want to use another MongoDB connection (not the main), you have to add the key of the connection with this param.

```
@CsModel(collectionName = "collection_name")
public class MyModel extends CsActiveRecord<MyModel> {
	
	@Required
	@Email
	public String email;

	@Required
	public String password;

	@Required
	public String not_a_robot;

	@Safe
	public Boolean remember_me;

}
```

#### Attributes

The attributes are used as in the active form. Private attributes are not used neither, so it will not be saved and it will not be filled.

If you want that one public attribute will not be saved, you can annotate it with @NotSave.

#### Behaviours

There are more annotations for more events in the active records:

- @BeforeSave
- @AfterSave
- @BeforeDelete
- @AfterDelete
- @BeforeFind
- @AfterFind
- @BeforeCount
- @AfterCount

#### Save

- **save:** It saves the full current model with all its attributes.
	* Validation: yes
	* Events: BeforeValidate, AfterValidate, BeforeSave, AfterSave
	```
	void save(Handler<AsyncResult<Boolean>> handler);
	```
- **saveAttributes:** It saves only a the indicated attributes of the current model.
	* Validation: no
	* Events: NONE
	```
	void saveAttributes(List<String> attributes, Handler<AsyncResult<Void>> handler);
	```
- **saveAttributes:** It saves a set of attributes of any model which matches with the query.
	* Validation: no
	* Events: NONE
	```
	void saveAttributes(List<String> attributes, JsonObject query, Handler<AsyncResult<Void>> handler);
	```
- **updateAllFree:** It executes [the MongoDB "update" command](https://docs.mongodb.com/manual/reference/method/db.collection.update/).
	* Validation: no
	* Events: NONE
	```
	void updateAllFree(JsonObject query, JsonObject update, Handler<AsyncResult<Boolean>> handler);
	```

#### Find

- **find:** It fetches the first model which matches with the query.
	* Events: BeforeFind, AfterFind
	```
	void find(JsonObject query, Handler<AsyncResult<T>> handler);

	void find(CsCriteria criteria, Handler<AsyncResult<T>> handler);
	```

- **findByPk:** It fetches the model with the indicated primary key (ID).
	* Events: BeforeFind, AfterFind
	```
	void findByPk(String id, Handler<AsyncResult<T>> handler);
	```

- **findAll:** It fetches all the models which match with the query.
	* Events: BeforeFind, AfterFind
	```
	void findAll(Handler<AsyncResult<List<T>>> handler);
	void findAll(JsonObject query, Handler<AsyncResult<List<T>>> handler);
	void findAll(CsCriteria criteria, Handler<AsyncResult<List<T>>> handler);
	void findAll(JsonObject query, FindOptions options, Handler<AsyncResult<List<T>>> handler);
	```

- **findAllByPks:** It fetches all the models which primary key is between the indicated ones.
	* Events: BeforeFind, AfterFind
	```
	void findAllByPks(List<String> ids, Handler<AsyncResult<List<T>>> handler);
	```


- **findDiffValues:** It get the different values that a field has in the model collection.
	* Events: NONE
	```
	void findDiffValues(String field, String fieldClass, Handler<AsyncResult<JsonArray>> handler);
	```

#### Delete

- **delete:** It deletes the current model.
	* Events: BeforeDelete, AfterDelete
	```
	void delete(Handler<AsyncResult<Boolean>> handler);
	```

- **deleteAll:** It deletes all the models which matches with the query.
	* Events: NONE
	```
	void deleteAll(JsonObject query, Handler<AsyncResult<Void>> handler);

	void deleteAll(CsCriteria criteria, Handler<AsyncResult<Void>> handler);
	```

- **deleteByPk:** It deletes the model with the indicated primary key (ID).
	* Events: NONE
	```
	void deleteByPk(String id, Handler<AsyncResult<Void>> handler);
	```

- **deleteAfterFind:** It finds the model which matches with the query and then it executes on the model the method _delete_.
	* Events: BeforeDelete, AfterDelete
	* Handler result: not deleted models
	```
	void deleteAfterFind(JsonObject query, Handler<AsyncResult<Boolean>> handler);

	void deleteAfterFind(JsonObject query, String scenario, Handler<AsyncResult<Boolean>> handler);
	```

- **deleteAfterFindByPk:** It finds the model with the indicated primary key (ID) and then it executes on the model the method _delete_.
	* Events: BeforeDelete, AfterDelete
	* Handler result: not deleted models
	```
	void deleteAfterFindByPk(String id, Handler<AsyncResult<Boolean>> handler);

	void deleteAfterFindByPk(String id, String scenario, Handler<AsyncResult<Boolean>> handler);
	```

- **deleteAllAfterFindAll:** It finds all the models which match with the query and then it executes on them the method _delete_.
	* Events: BeforeDelete, AfterDelete
 	* Handler result: not deleted models
	```
	void deleteAllAfterFindAll(JsonObject query, Handler<AsyncResult<List<T>>> handler);

	void deleteAllAfterFindAll(CsCriteria criteria, Handler<AsyncResult<List<T>>> handler);

	void deleteAllAfterFindAll(JsonObject query, String scenario, Handler<AsyncResult<List<T>>> handler);
	```

- **deleteAttributes:** It removes a set of attributes from the current model.
	* Events: NONE
	```
	void deleteAttributes(List<String> attributes, Handler<AsyncResult<Void>> handler);
	```

- **deleteAttributes:** It removes a set of attributes from any model which matches with the query.
	* Events: NONE
	```
	void deleteAttributes(List<String> attributes, JsonObject query, Handler<AsyncResult<Boolean>> handler);
	```

#### Others

- **count:** It returns the number of documents which match with the query.
	* Events: BeforeCount, AfterCount
	```
	void count(Handler<AsyncResult<Long>> handler);

	void count(JsonObject query, Handler<AsyncResult<Long>> handler);

	void count(CsCriteria criteria, Handler<AsyncResult<Long>> handler);
	```

- **exists:** It returns _true_ if it exists some document which matches with the query. Otherwise, it returns _false_.
	* Events: NONE
	```
	void exists(JsonObject query, Handler<AsyncResult<Boolean>> handler);

	void exists(CsCriteria criteria, Handler<AsyncResult<Boolean>> handler);

	void exists(Handler<AsyncResult<Boolean>> handler);
	```

- **search:** It returns a CsDbDataProvider with a search in the collection. The searchable attributes have to be annotated with **@Searchable**.
	* Events: NONE
	```
	CsDbDataProvider<T> search(MultiMap queryParams);

	CsDbDataProvider<T> search(MultiMap queryParams, Integer pageSize);

	CsDbDataProvider<T> search(MultiMap queryParams, Integer defaultPageSize, JsonObject defaultSort);
	```

- **aggregate:** It executes [the MongoDB "aggregation" command](https://docs.mongodb.com/manual/aggregation/).
	* Events: NONE
	```
	void aggregate(JsonArray pipeline, Handler<AsyncResult<JsonArray>> handler);
	```

### Data Provider

The class **CsDbDataProvider** contains a paginated search for a model type.

```
CsCriteria criteria = new CsCriteria()
	.addCondition("city", "Seville");
CsDbDataProvider<User> dataProvider = new CsDbDataProvider<User>(User.class, criteria);
dataProvider.setCurrentPage(5);
dataProvider.getData(res -> {
	...
	int numPages = dataProvider.getPageCount();
	long numItems = dataProvider.getItemCount();
	...
});
```

The class has the following methods:
- **```CsCriteria getCriteria()```:** It gets the criteria used in the search.
- **```CsDbDataProvider<T> setCriteria(CsCriteria criteria)```:** It set the criteria used in the search.
- **```CsPagination<T> getPagination()```:** It gets the pagination data of the data provider.
- **```void setPagination(CsPagination<T> pagination)```:**  It sets the pagination data of the data provider.
- **```int getPageSize()```:**  It gets the number of items in a page.
- **```int getCurrentPage()```:** It gets the current page number.
- **```Integer getPageCount()```:** It gets the number of pages.
- **```boolean hasPreviousPage()```:** It returns if there is a previous page.
- **```boolean hasNextPage()```:** It returns if there is a next page.
- **```CsDataProvider<T> setCurrentPage(int page)```:** It sets the current page for the search.
- **```void getData(Handler<AsyncResult<List<T>>> handler)```:** It returns the models of the current page and it initializes the pagination data.

#### Pagination

The class **CsPagination<T>** has the pagination info of a data provider.



## Contributing

Do you want to contribute? It will be helpful :smile:. Write me and I tell you what are we improving now: yo@rogelioorts.com.


## License

[MIT](LICENSE.md)