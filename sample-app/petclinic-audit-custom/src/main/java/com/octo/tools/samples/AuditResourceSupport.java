package com.octo.tools.samples;

import org.hibernate.envers.RevisionType;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.CustomRevisionEntity;

public class AuditResourceSupport<T extends BaseEntity> extends com.octo.tools.audit.AuditResourceSupport<T> {

	private String userName;

	public AuditResourceSupport(RevisionType revType, T entity, CustomRevisionEntity revEntity) {
		super(entity, entity.getId().longValue(), revEntity.getId(), revEntity.getTimestamp(), revType);
		this.userName = revEntity.getUserName();
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	

	
	
	
}
