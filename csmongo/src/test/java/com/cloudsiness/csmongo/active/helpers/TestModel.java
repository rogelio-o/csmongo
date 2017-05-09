package com.cloudsiness.csmongo.active.helpers;

import java.util.List;

import com.cloudsiness.csmongo.active.CsActiveRecord;
import com.cloudsiness.csmongo.active.annotations.CsModel;
import com.cloudsiness.csmongo.active.annotations.Safe;
import com.cloudsiness.csmongo.active.annotations.events.AfterCount;
import com.cloudsiness.csmongo.active.annotations.events.AfterDelete;
import com.cloudsiness.csmongo.active.annotations.events.AfterFind;
import com.cloudsiness.csmongo.active.annotations.events.AfterSave;
import com.cloudsiness.csmongo.active.annotations.events.BeforeCount;
import com.cloudsiness.csmongo.active.annotations.events.BeforeDelete;
import com.cloudsiness.csmongo.active.annotations.events.BeforeFind;
import com.cloudsiness.csmongo.active.annotations.events.BeforeSave;
import com.cloudsiness.csmongo.active.validators.required.Required;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@CsModel(collectionName="test")
public class TestModel extends CsActiveRecord<TestModel> {
	public String attribute1;
	
	@Safe
	@Required
	public String safeAttribute;
	
	@JsonIgnore
	public String ignoredAttribute;
	
	public String idBelongsToRelation;
	
	public String idHasOneRelation;
	
	public String idHasManyRelation;
	
	public List<String> idsManyManyRelation;
	
	public JsonObject query;
	
	private boolean beforeSaveCalled = false;
	private boolean beforeSaveCalled2 = false;
	private boolean afterSaveCalled = false;
	private boolean beforeFindCalled = false;
	private boolean afterFindCalled = false;
	private boolean beforeCountCalled = false;
	private boolean afterCountCalled = false;
	private boolean beforeDeleteCalled = false;
	private boolean afterDeleteCalled = false;
	
	private boolean beforeSaveResult = true;
	private boolean afterSaveResult = true;
	private boolean beforeDeleteResult = true;
	private boolean afterDeleteResult = true;
	
	
	public TestModel() {
		super();
		
		beforeSaveCalled = false;
		beforeSaveCalled2 = false;
		afterSaveCalled = false;
		beforeFindCalled = false;
		afterFindCalled = false;
		beforeCountCalled = false;
		afterCountCalled = false;
		beforeDeleteCalled = false;
		afterDeleteCalled = false;
	}
	
	public TestModel(String attribute1, String safeAttribute) {
		this();
		
		this.attribute1 = attribute1;
		this.safeAttribute = safeAttribute;
	}
	
	@BeforeSave
	public void beforeSave(Handler<AsyncResult<Boolean>> handler) {
		beforeSaveCalled = true;
		handler.handle(Future.succeededFuture(beforeSaveResult));
	}
	
	@BeforeSave
	public void beforeSave2(Handler<AsyncResult<Boolean>> handler) {
		beforeSaveCalled2 = true;
		handler.handle(Future.succeededFuture(true));
	}
	
	@AfterSave
	public void afterSave(Handler<AsyncResult<Boolean>> handler) {
		afterSaveCalled = true;
		handler.handle(Future.succeededFuture(afterSaveResult));
	}
	
	@BeforeFind
	public void beforeFind(Handler<AsyncResult<Boolean>> handler) {
		beforeFindCalled = true;
		handler.handle(Future.succeededFuture(true));
	}
	
	@AfterFind
	public void afterFind(Handler<AsyncResult<Boolean>> handler) {
		afterFindCalled = true;
		handler.handle(Future.succeededFuture(true));
	}
	
	@BeforeCount
	public void beforeCount(Handler<AsyncResult<Boolean>> handler) {
		beforeCountCalled = true;
		handler.handle(Future.succeededFuture(true));
	}
	
	@AfterCount
	public void afterCount(Handler<AsyncResult<Boolean>> handler) {
		afterCountCalled = true;
		handler.handle(Future.succeededFuture(true));
	}
	
	@BeforeDelete
	public void beforeDelete(Handler<AsyncResult<Boolean>> handler) {
		beforeDeleteCalled = true;
		handler.handle(Future.succeededFuture(beforeDeleteResult));
	}
	
	@AfterDelete
	public void afterDelete(Handler<AsyncResult<Boolean>> handler) {
		afterDeleteCalled = true;
		handler.handle(Future.succeededFuture(afterDeleteResult));
	}
	
	
	

	public void setBeforeSaveResult(boolean beforeSaveResult) {
		this.beforeSaveResult = beforeSaveResult;
	}

	public void setAfterSaveResult(boolean afterSaveResult) {
		this.afterSaveResult = afterSaveResult;
	}

	public void setBeforeDeleteResult(boolean beforeDeleteResult) {
		this.beforeDeleteResult = beforeDeleteResult;
	}

	public void setAfterDeleteResult(boolean afterDeleteResult) {
		this.afterDeleteResult = afterDeleteResult;
	}

	public boolean isBeforeSaveCalled() {
		return beforeSaveCalled;
	}

	public boolean isBeforeSaveCalled2() {
		return beforeSaveCalled2;
	}

	public boolean isAfterSaveCalled() {
		return afterSaveCalled;
	}

	public boolean isBeforeFindCalled() {
		return beforeFindCalled;
	}

	public boolean isAfterFindCalled() {
		return afterFindCalled;
	}

	public boolean isBeforeCountCalled() {
		return beforeCountCalled;
	}

	public boolean isAfterCountCalled() {
		return afterCountCalled;
	}

	public boolean isBeforeDeleteCalled() {
		return beforeDeleteCalled;
	}

	public boolean isAfterDeleteCalled() {
		return afterDeleteCalled;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestModel other = (TestModel) obj;
		if (attribute1 == null) {
			if (other.attribute1 != null)
				return false;
		} else if (!attribute1.equals(other.attribute1))
			return false;
		if (safeAttribute == null) {
			if (other.safeAttribute != null)
				return false;
		} else if (!safeAttribute.equals(other.safeAttribute))
			return false;
		return true;
	}
	
}