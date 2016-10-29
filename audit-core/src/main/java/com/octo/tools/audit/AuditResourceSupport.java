package com.octo.tools.audit;

import org.hibernate.envers.RevisionType;
import org.springframework.hateoas.ResourceSupport;

public class AuditResourceSupport<T> extends ResourceSupport {

	protected T entity;
	
	protected Long entityId;
	
	protected Long revId;

	protected Long revTimestamp;
	
	protected RevisionType revType;
	
	public AuditResourceSupport(T entity, Long entityId, Long revEntityId, Long revEntityTimestamp, RevisionType revType) {
		super();
		this.entity = entity;
		this.entityId = entityId;
		this.revId = revEntityId;
		this.revTimestamp = revEntityTimestamp;
		this.revType = revType;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T data) {
		this.entity = data;
	}


	

	public RevisionType getRevType() {
		return revType;
	}

	public void setRevType(RevisionType revType) {
		this.revType = revType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public Long getRevId() {
		return revId;
	}

	public void setRevId(Long revId) {
		this.revId = revId;
	}

	public Long getRevTimestamp() {
		return revTimestamp;
	}

	public void setRevTimestamp(Long revTimestamp) {
		this.revTimestamp = revTimestamp;
	}

	
}
