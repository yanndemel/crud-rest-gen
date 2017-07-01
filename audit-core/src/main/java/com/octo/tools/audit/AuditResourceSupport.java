package com.octo.tools.audit;

import java.util.Date;

import org.hibernate.envers.RevisionType;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AuditResourceSupport<T> extends ResourceSupport {

	protected T entity;
	
	protected Long entityId;
	
	protected Long revId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	protected Date revTimestamp;
	
	protected RevisionType revType;
	
	public AuditResourceSupport(T entity, Long entityId, Long revEntityId, Date revEntityTimestamp, RevisionType revType) {
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

	public Date getRevTimestamp() {
		return revTimestamp;
	}

	public void setRevTimestamp(Date revTimestamp) {
		this.revTimestamp = revTimestamp;
	}

	
}
