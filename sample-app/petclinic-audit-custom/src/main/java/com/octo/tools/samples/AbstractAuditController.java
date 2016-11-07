package com.octo.tools.samples;

import org.hibernate.envers.RevisionType;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.CustomRevisionEntity;

public abstract class AbstractAuditController<T extends BaseEntity> extends com.octo.tools.audit.AbstractAuditController<T, CustomRevisionEntity> {

    public AbstractAuditController(Class<T> entityClass, Class<? extends AbstractAuditController<T>> controller) {
		super(entityClass, controller);
	}

	@Override
	protected AuditResourceSupport<T> newAuditResourceSupport(RevisionType revType, T entity,
			CustomRevisionEntity revEntity) {
		return new AuditResourceSupport<T>(revType, entity, revEntity);
	}

	@Override
	protected Long getRevisionEntityId(CustomRevisionEntity revEntity) {
		return revEntity.getId();
	}

	@Override
	protected Long getRevisionEntityTimestamp(CustomRevisionEntity revEntity) {
		return revEntity.getTimestamp();
	}

	@Override
	protected Long getEntityId(T entity) {
		return entity.getId();
	}




	
	
	
	 


	
	
}
