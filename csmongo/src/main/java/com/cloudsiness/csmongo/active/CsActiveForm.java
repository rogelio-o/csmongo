package com.cloudsiness.csmongo.active;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.types.ObjectId;

import com.cloudsiness.csmongo.active.annotations.Safe;
import com.cloudsiness.csmongo.active.annotations.Searchable;
import com.cloudsiness.csmongo.active.annotations.events.AfterValidate;
import com.cloudsiness.csmongo.active.annotations.events.BeforeValidate;
import com.cloudsiness.csmongo.active.exceptions.CsConstraintAnnotationWithoutScenarios;
import com.cloudsiness.csmongo.active.hooks.CsHookHandler;
import com.cloudsiness.csmongo.active.validators.CsConstraint;
import com.cloudsiness.csmongo.active.validators.CsConstraintValidator;
import com.cloudsiness.csmongo.active.validators.CsValidationError;
import com.cloudsiness.csmongo.active.validators.CsValidator;
import com.cloudsiness.csmongo.active.validators.required.Required;
import com.cloudsiness.csmongo.helpers.CsFutureHelper;
import com.cloudsiness.csmongo.helpers.CsMultiMapHelper;
import com.cloudsiness.csmongo.helpers.CsReflectionHelper;
import com.cloudsiness.csmongo.helpers.pojo.CsPojoHelper;
import com.cloudsiness.csmongo.helpers.serializers.ObjectIdDbJsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The classes that extend this abstract class will have the functionalities of 
 * an active record less the database functionalities.
 * 
 * @author 		Rogelio R. Orts Cansino
 * @version		0.1
 * @param <T>	The type of the superclass (the class that will have the active form functionalities).
 */
public abstract class CsActiveForm<T extends CsActiveForm<T>> {
	
	// CONSTANTS -------
	
	/**
	 * Constant name for the main scenario. Helpful in validations.
	 */
	public static final String MAIN_SCENARIO = "MAIN_SCENARIO";
	
	/**
	 * Constant name for event "on validate find".
	 */
	public static final String ON_BEFORE_VALIDATE = "onBeforeValidate";
	
	/**
	 * Constant name for event "on after validate".
	 */
	public static final String ON_AFTER_VALIDATE = "onAfterValidate";
	
	/**
	 * Constant key for general errors. General errors are errors that are not attached to an attribute.
	 */
	public static final String GENERAL_ERRORS = "GENERAL_ERRORS";
	
	
	// ATTRIBUTES -------
	
	/**
	 * The Class of the supper class. Needed to get the annotations.
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> modelType = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	
	/**
	 * Saves the errors when the model is validated.
	 */
	protected Map<String, List<CsValidationError>> errors;
	
	/**
	 * Saves the current scenario of the active form for the validation.
	 */
	private String scenario;
	
	
	// CONTEXT DATA -------
	
	protected Logger logger;
	
	
	// HOOKS FIELDS -------
	
	/**
	 * Hooks registered for to be called on before validate.
	 */
	protected List<CsHookHandler<T>> hooksOnBeforeValidate;
	
	/**
	 * Hooks registered for to be called on after validate.
	 */
	protected List<CsHookHandler<T>> hooksOnAfterValidate;
	
	
	// CONSTRUCTORS -------
	
	protected CsActiveForm() {
		this.hooksOnBeforeValidate = new LinkedList<CsHookHandler<T>>();
		this.hooksOnAfterValidate = new LinkedList<CsHookHandler<T>>();
		
		this.errors = new HashMap<String, List<CsValidationError>>();
		this.scenario = MAIN_SCENARIO;
		
		this.logger = LoggerFactory.getLogger(modelType);
	}
	
	
	// HELPER METHODS -------
	
	/**
	 * Get all the methods that has the annotation listed in parameters.
	 * 
	 * @param annotation	the annotation that the methods should have.
	 * @return				a list of the methods with the listed annotation.
	 */
	protected List<Method> getMethodsAnnotedWith(Class<? extends Annotation> annotation) {
		List<Method> result = new LinkedList<Method>();
		
		for(Method m : modelType.getMethods()) {
			if(m.isAnnotationPresent(annotation)) {
				result.add(m);
			}
		}
				
		return result;
	}
	
	/**
	 * @param safe	if the fields must have the @Safe annotation or some validator annotation to be included.
	 * @return		a list with all the public fields (attributes of the model).
	 */
	public List<Field> getFields(boolean safe, String scenario) {
		List<Field> result = new LinkedList<Field>();

		for(Field f : getAllFields(new LinkedList<Field>(), modelType)) {
			if(
				Modifier.toString(f.getModifiers()).equals("public")
					&& 
				(
					!safe 
						|| 
					(safe && isSafeAttribute(f, scenario))
				)
			) {
				result.add(f);
			}
		}
		
		return result;
	}
	
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        fields = getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}
	
	/**
	 * @return	a list with the public fields (can be only the safe ones) in the current validation scenario.
	 * @see 	#getFields(boolean, String)
	 */
	public List<Field> getFields(boolean safe) {
		return getFields(safe, getScenario());
	}
	
	/**
	 * @return	a list with all the public fields (safe and not safes).
	 * @see 	#getFields(boolean, String)
	 */
	public List<Field> getFields() {
		return getFields(false, getScenario());
	}
	
	/**
	 * @return	a list with all the safes attributes in the current validation scenario.
	 * @see 	#getFields(boolean, String)
	 */
	public List<Field> getSafeFields() {
		return getFields(true, getScenario());
	}
	
	/**
	 * @param attribute	the attribute to looking for the value.
	 * @return			the value of the attribute.
	 */
	@SuppressWarnings("unchecked")
	public <S> S getAttributeValue(String attribute) {
		try {
			Field f = modelType.getField(attribute);
			return (S) f.get(this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
		
	}
	
	/**
	 * A field is safe if it has the annotation @Safe or some validator annotation.
	 * 
	 * @param f	the field that we want know if it is safe.
	 * @return	<code>true</code> if the field is safe.
	 */
	private boolean isSafeAttribute(Field f, String scenario) {
		return isAnnotationSafePresent(f, scenario) || hasAttributeValidators(f, scenario);
	}
	
	/**
	 * Returns if the attributed is annotated as safe for the scenario listed in parameters.
	 * 
	 * @param f			the field that is looking for.
	 * @param scenario	the scenario that the safe annotation must contains.
	 * @return			<code>true</code> if the field is annotated as safe.
	 */
	private boolean isAnnotationSafePresent(Field f, String scenario) {
		return f.isAnnotationPresent(Safe.class) && isAnnotationInScenario(f.getAnnotation(Safe.class), scenario);
	}
	
	/**
	 * @param f	the field for find out if it has validators.
	 * @return	<code>true</code> if the field has validators.s
	 */
	private boolean hasAttributeValidators(Field f, String scenario) {
		return !getValidators(f, scenario).isEmpty();
	}
	
	/**
	 * Get the annotated validations in the field and in the scenario that are listed as parameters.
	 * <p>
	 * If the field is null, then the validators will be the validators annotated in the modelType.
	 * 
	 * @param f			null or the field for to looking for validators.
	 * @param scenario	the scenario that the validators must include.
	 * @return			a list of validators for the attribute/model in the scenario.
	 */
	private List<CsValidator> getValidators(Field f, String scenario) {
		List<CsValidator> validators = new ArrayList<CsValidator>();
		Annotation[] anns = f != null ? f.getAnnotations() : modelType.getAnnotations();
		
		for(Annotation ann : anns) {
			CsConstraint constraint = ann.annotationType().getAnnotation(CsConstraint.class);
			if(constraint != null) {
				if(isAnnotationInScenario(ann, scenario)) {
					for(Class<? extends CsConstraintValidator<?>> c : constraint.validatedBy())
						validators.add(CsValidator.create(c, ann, scenario));
				}
			}
		}
		
		return validators;
	}
	
	/**
	 * Get the validators annotated in the modelType.
	 * 
	 * @see #getValidators(Field, String)
	 */
	private List<CsValidator> getValidators(String scenario) {
		return getValidators(null, scenario);
	}
	
	/**
	 * Invokes the method scenarios() to know in which scenarios are the validator available.
	 * <p>
	 * If the annotation has no method scenarios(), then returns <code>false</code>.
	 * 
	 * @param ann		the annotation to know if is in the scenario.
	 * @param scenario	the scenario to check.
	 * @return			<code>true</code> if the validator is available in the scenario listed in the parameters.
	 */
	private boolean isAnnotationInScenario(Annotation ann, String scenario) {
		try {
			for(String s : getScenarios(ann)) {
				if(s.equals(scenario) || s.equals(MAIN_SCENARIO))
					return true;
			}
		} catch (CsConstraintAnnotationWithoutScenarios e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Gets the available scenarios for a constraint in an annotation.
	 * 
	 * @param ann	the annotation for to looking for the scenarios.
	 * @return		a list of the available scenarios.
	 * @throws CsConstraintAnnotationWithoutScenarios
	 */
	private String[] getScenarios(Annotation ann) throws CsConstraintAnnotationWithoutScenarios {
		try {
			Method m = ann.getClass().getMethod("scenarios");
			return (String[]) m.invoke(ann);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CsConstraintAnnotationWithoutScenarios(ann.annotationType().getName());
		}
	}
	
	
	// ON EVENT METHODS -------
	
	/**
	 * Calls asynchronously the methods annotated with the annotationClass and invokes the hooks listed in parameters. 
	 * When all the methods and all the hooks are executed, invokes the handler with the result of all the executions 
	 * (<code>true</code> or <code>false</code>).
	 * 
	 * @param annotationClass		the annotation class that will have the methods called.
	 * @param hooks					the hooks that will be invoked.
	 * @param handler				the handler to run when all the methods and hooks are executed.
	 */
	@SuppressWarnings("unchecked")
	protected void callOn(Class<? extends Annotation> annotationClass, List<CsHookHandler<T>> hooks, Handler<AsyncResult<Boolean>> handler) {
		List<Method> methods = getMethodsAnnotedWith(annotationClass);
		int total = methods.size() + hooks.size();
		AtomicInteger count = new AtomicInteger(0);
		AtomicBoolean result = new AtomicBoolean(true);
		AtomicBoolean error = new AtomicBoolean(false);
		Handler<AsyncResult<Boolean>> resultHandler = res -> {
			if(!error.get()) {
				if(res.failed()) {
					error.set(false);
					handler.handle(Future.failedFuture(res.cause()));
				} else {
					if(!res.result())
						result.set(false);
					
					if(count.incrementAndGet() == total) {
						handler.handle(Future.succeededFuture(result.get()));
					}
				}
			}
		};
		
		if(total == 0) {
			handler.handle(Future.succeededFuture(result.get()));
		} else {
			try {
				// Run methods with annotation
				for(Method m : getMethodsAnnotedWith(annotationClass)) {
					m.invoke(this, resultHandler);
				}
				
				// Run hooks
				for(CsHookHandler<T> h : hooks) {
					h.handle((T) this, resultHandler);
				}
			} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException  e) {
				handler.handle(Future.failedFuture(e));
			}
		}
	}
	
	/**
	 * Call the methods annotated with @BeforeValidate and the hooks of the field hooksOnBeforeValidate.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnBeforeValidate(Handler<AsyncResult<Boolean>> handler) {
		callOn(BeforeValidate.class, hooksOnBeforeValidate, handler);
	}
	
	/**
	 * Call the methods annotated with @AfterValidate and the hooks of the field hooksOnAfterValidate.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected void callOnAfterValidate(Handler<AsyncResult<Boolean>> handler) {
		callOn(AfterValidate.class, hooksOnAfterValidate, handler);
	}
	
	/**
	 * Register hooks to an event.
	 * 
	 * @param where		the event where register the hook.
	 * @param handler	the handler for to run when the event happen.
	 */
	public void registerHook(String where, CsHookHandler<T> handler) {
		switch(where) {
			case ON_BEFORE_VALIDATE:
				hooksOnBeforeValidate.add(handler);
				break;
			case ON_AFTER_VALIDATE:
				hooksOnAfterValidate.add(handler);
				break;
		}
	}
	
	
	// ERRORS METHODS -------
	
	public Map<String, List<CsValidationError>> getErrors() {
		return errors;
	}
	
	public List<CsValidationError> getErrors(String attribute) {
		return errors.get(attribute);
	}
	
	public List<CsValidationError> getErrorsAsList() {
		List<CsValidationError> result = new ArrayList<CsValidationError>();
		
		if(errors == null)
			return result;
		
		for(Map.Entry<String, List<CsValidationError>> entry : errors.entrySet())
			result.addAll(entry.getValue());
			
		return result;
	}
	
	public void setErrors(Map<String, List<CsValidationError>> errors) {
		this.errors = errors;
	}
	
	public void setErrors(JsonObject errors) {
		if(this.errors == null)
			this.errors = new HashMap<String, List<CsValidationError>>();
		for(Entry<String, Object> entry : errors) {
			JsonArray attributeErrors = (JsonArray) entry.getValue();
			List<CsValidationError> errorsList = this.errors.get(entry.getKey());
			if(errorsList == null) {
				errorsList = new ArrayList<CsValidationError>();
				this.errors.put(entry.getKey(), errorsList);
			}
			
			for(Object attributeError : attributeErrors) {
				errorsList.add(new CsValidationError((String) attributeError, entry.getKey()));
			}
		}
	}
	
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public boolean hasErrors(String attribute) {
		return errors.get(attribute) != null && errors.get(attribute).size() > 0;
	}
	
	public void addError(String attribute, String error) {
		List<CsValidationError> attributeErrors = errors.get(attribute);
		if(attributeErrors == null) {
			attributeErrors = new LinkedList<CsValidationError>();
			errors.put(attribute, attributeErrors);
		}
		
		attributeErrors.add(new CsValidationError(error, attribute));
	}
	
	public void addError(String attribute, CsValidationError error) {
		List<CsValidationError> attributeErrors = errors.get(attribute);
		if(attributeErrors == null) {
			attributeErrors = new LinkedList<CsValidationError>();
			errors.put(attribute, attributeErrors);
		}
		
		attributeErrors.add(error);
	}
	
	public void addErrors(String attribute, List<String> newErrors) {
		List<CsValidationError> attributeErrors = errors.get(attribute);
		if(attributeErrors == null) {
			attributeErrors = new LinkedList<CsValidationError>();
			errors.put(attribute, attributeErrors);
		}
		
		for(String error : newErrors)
			attributeErrors.add(new CsValidationError(error, attribute));
	}
	
	public void addErrors(Map<String,List<CsValidationError>> newErrors) {
		if(newErrors != null)
			errors.putAll(newErrors);
	}
	
	public void clearErrors() {
		errors.clear();
	}
	
	
	// SET ATTRIBUTES -------
	
	protected SimpleModule getCustomDeserializers() {
		return null;
	}
	
	protected SimpleModule getCustomSerializers() {
		return null;
	}
	
	/**
	 * Sets the attributes of the object with the values on the JSON object listed in parameters.
	 * 
	 * @param safes		if the set attributes are only the safe attributes
	 * @param body		the values of the attributes
	 */
	protected void setAttributes(boolean safes, JsonObject body) {
		if(body == null)
			body = new JsonObject();
		
		JsonObject endBody = new JsonObject();
		
		for(Field f : getFields(safes)) {
			String fieldName = f.getName();
			if(body.containsKey(fieldName)) {
				endBody.put(fieldName, body.getValue(fieldName));
			}
			
			if(f.getType().isAssignableFrom(Date.class)) {
				if(body.containsKey(fieldName + "_date")) {
					endBody.put(fieldName + "_date", body.getValue(fieldName + "_date"));
				}
				
				if(body.containsKey(fieldName + "_time")) {
					String time = body.getString(fieldName + "_time");
					if(time != null && !time.isEmpty())
						endBody.put(fieldName + "_time", time);
					else
						endBody.put(fieldName + "_time", " 00:00");
				}
			} else if(Collection.class.isAssignableFrom(f.getType()) || f.getType().equals(JsonArray.class)) {
				if(body.getValue(fieldName) != null && body.getValue(fieldName).getClass().equals(String.class) && body.getString(fieldName).isEmpty()) {
					endBody.putNull(fieldName);
				}
			}
		}
		
		try {
			CsPojoHelper.deserialize(this, endBody, getCustomDeserializers());
		} catch(Exception e) {
			logger.error("CsActiveForm - setAttributes", e);
		}
	}
	
	/**
	 * Sets the safe attributes.
	 * 
	 * @see #setAttributes(boolean, JsonObject)
	 */
	public void setAttributes(JsonObject body) {
		setAttributes(true, body);
	}
	
	public void setAttributes(MultiMap params) {
		setAttributes(true, CsMultiMapHelper.helper().toJson(params));
	}
	
	public void setAttribute(String attribute, Object value) {
		try {
			Field f = modelType.getField(attribute);
			f.set(this, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error("Error in setAttribute " + modelType.getSimpleName(), e);
		}
	}
	
	/**
	 * Creates a JsonObject with the attributes passed in parameters as keys and
	 * the values of this attributes in the current model as values. If the attributes
	 * are not passed as parameter (null), all the attributes of the model will be used.
	 * 
	 * @param attributes	the attributes to retrieve the values.
	 * @return				a JSON object of attributes - values.
	 * @throws JsonProcessingException 
	 */
	protected JsonObject getAttributes(List<String> attributes, boolean emptyValues, boolean toSave) throws JsonProcessingException {
		SimpleModule customModule = getCustomSerializers() != null ? getCustomSerializers() : new SimpleModule();
		if(toSave)
			customModule.addSerializer(ObjectId.class, new ObjectIdDbJsonSerializer());
		
		JsonObject result = CsPojoHelper.serialize(this, emptyValues, customModule);
		
		if(attributes != null) {
			JsonObject newResult = result.copy();
			result.forEach(item -> {
				if(!attributes.contains(item.getKey()))
					newResult.remove(item.getKey());
			});
			result = newResult;
		}
		
		return result;
	}
	
	public JsonObject getAttributes(List<String> attributes, boolean emptyValues) throws JsonProcessingException {
		return getAttributes(attributes, emptyValues, false);
	}
	
	public JsonObject getAttributes(List<String> attributes) throws JsonProcessingException {
		return getAttributes(attributes, true);
	}
	
	/**
	 * Returns all the attributes as JSON object.
	 * 
	 * @throws JsonProcessingException 
	 * @see #getAttributes(List)
	 */
	public JsonObject getAttributes() throws JsonProcessingException {
		return getAttributes(null, true);
	}
	
	public JsonObject getAttributes(boolean emptyValues) throws JsonProcessingException {
		return getAttributes(null, emptyValues);
	}
	
	
	// VALIDATION -------
	
	/**
	 * Validates that the attributes have not errors with the indicated validators in the attributes through annotations.
	 * 
	 * @return	<code>true</code> if any attribute has error.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void validate(Handler<AsyncResult<Boolean>> handler) {
		// Clear errors of a previous validation
		clearErrors();
		
		// Fire event on before validate
		callOnBeforeValidate(res -> {
			if(res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
			} else {
				Boolean result = res.result();
				if(!result) {
					handler.handle(Future.succeededFuture(false));
				} else {
					List<CsValidator> validators = getValidators(getScenario());
					Map<Field, List<CsValidator>> fieldsValidators = new HashMap<Field, List<CsValidator>>();
					
					for(Field field : getFields()) {
						List<CsValidator> fieldValidators = fieldsValidators.get(field);
						if(fieldValidators == null) {
							fieldValidators = new ArrayList<CsValidator>();
							fieldsValidators.put(field, fieldValidators);
						}
						
						for(CsValidator validator : getValidators(field, getScenario())) {
							fieldValidators.add(validator);
						}
					}
					
					Handler<AsyncResult<Boolean>> validationHandler = valRes -> {
						if(valRes.failed()) {
							handler.handle(Future.failedFuture(valRes.cause()));
						} else {
							if(!valRes.result()) {
								handler.handle(Future.succeededFuture(false));
							} else {
								// Fire event on after validate and if is not ok returns false
								
								callOnAfterValidate(res2 -> {
									if(res2.failed()) {
										handler.handle(Future.failedFuture(res2.cause()));
									} else {
										handler.handle(Future.succeededFuture(res2.result()));
									}
								});
							}
						}
					};

					Future<Void> fGeneral = Future.future();
					CsFutureHelper.helper().doInItemsVoid(validators, (validator, f) -> {
						try {
							validator.validate(this, null, valRes -> {
								if(valRes.failed()) {
									f.fail(valRes.cause());
									return;
								} 
								
								CsValidationError valResError = valRes.result();
								if(valResError != null) {
									addError(GENERAL_ERRORS, valResError);
										
									f.complete();
								} else {
									f.complete();
								}
							});
						} catch (Exception e) {
							f.fail(e);
						}
					}, fGeneral.completer());
					
					Future<Void> fFields = Future.future();
					CsFutureHelper.helper().doInItemsVoid(fieldsValidators.entrySet(), (entry, f) -> {
						Field field = entry.getKey();
						
						CsFutureHelper.helper().doInItemsVoid(entry.getValue(), (validator, f2) -> {
							try {
								validator.validate(this, field.getName(), valRes -> {
									if(valRes.failed()) {
										f2.fail(valRes.cause());
										return;
									}
									
									CsValidationError valResError = valRes.result();
									if(valResError != null) {
										addError(field.getName(), valResError);
											
										f2.complete();
									} else {
										f2.complete();
									}
								});
							} catch (Exception e) {
								f2.fail(e);
							}
						}, f.completer());
					}, fFields.completer());
					
				
					CompositeFuture.all(fGeneral, fFields).setHandler(cRes -> {
						if(cRes.failed()) {
							validationHandler.handle(Future.failedFuture(cRes.cause()));
							return;
						}
						
						validationHandler.handle(Future.succeededFuture(!hasErrors()));
					});
				}
			}
		});
	}
	
	
	// GET REQUIRED FIELDS
	
	public List<String> getRequiredFields() {
		List<String> result = new ArrayList<String>();
		
		for(Field field : getSafeFields()) {
			Annotation ann = field.getAnnotation(Required.class);
			if(ann != null) {
				if(isAnnotationInScenario(ann, scenario)) {
					result.add(field.getName());
				}
			}
		}
		
		return result;
	}
	
	
	// SEARCH
	
	protected JsonObject searchFilters(JsonObject filters) {
		JsonObject query = new JsonObject();
		setAttributes(false, filters);
		
		for(Field f : getFields(false, null)) {
			if(f.isAnnotationPresent(Searchable.class)) {
				Searchable annotation = f.getAnnotation(Searchable.class);
				
				if(annotation.type() == Searchable.EXACT) {
					try {
						if(f.getType().isAssignableFrom(CsActiveForm.class)) {
							JsonObject sub = new JsonObject();
							
							for(Map.Entry<String, Object> entry : filters) {
								if(entry.getKey().startsWith(f.getName() + ".")) {
									String key = entry.getKey().substring(entry.getKey().indexOf(".") + 1);
									sub.put(key, entry.getValue());
								}
							}

							if(!sub.isEmpty()) {
								CsActiveForm<?> aux;
								aux = (CsActiveForm<?>) CsReflectionHelper.helper().resolveParameterizedType(f, getClass())
										.getConstructor().newInstance();
								JsonObject subFilters = aux.searchFilters(sub);
								
								for(Map.Entry<String, Object> entry : subFilters)
									query.put(f.getName() + "." + entry.getKey(), entry.getValue());
							}
						} else if(filters.containsKey(f.getName())) {
							Object value = f.get(this);
							
							value = value != null ? value : filters.getValue(f.getName());
							if(value != null && value.getClass().equals(Date.class))
								query.put(f.getName(), ((Date) value).getTime());
							else
								query.put(f.getName(), value);
						}
					} catch (Exception e) {
						logger.error("Field can not be set (exact) in search filters: " + f.getName(), e);
					}
				} else {
					if(filters.containsKey(f.getName())) {
						try {
							Object value = f.get(this);
							
							JsonObject singleQuery = new JsonObject()
									.put("$regex", value != null ? value : null)
									.put("$options", !annotation.options().isEmpty() ? annotation.options() : "i");
							
							query.put(f.getName(), singleQuery);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							logger.error("Field can not be set in search filters: " + f.getName(), e);
						}
					}
				}
			}
		}

		if(filters.containsKey("_ids")) {
			JsonArray ids = new JsonArray();
			for(Object id : filters.getString("_ids").split(",")) {
				ids.add(new JsonObject()
						.put("$oid", id)
					);
			}
			query.put("_id", new JsonObject()
					.put("$in", ids)
				);
		}
		
		return query;
	}
	
	
	// GETTERS & SETTERS -------
	
	public void setScenario(String scenario) {
		this.scenario = scenario;
	}
	
	public String getScenario() {
		return scenario;
	}
	
}
