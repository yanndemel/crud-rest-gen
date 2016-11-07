package com.octo.tools.samples;

import org.springframework.samples.petclinic.model.BaseEntity;

import com.octo.tools.audit.AbstractDefaultAuditController;

public abstract class AbstractAuditController<T extends BaseEntity> extends AbstractDefaultAuditController<T> {

    public AbstractAuditController(Class<T> entityClass, Class<? extends AbstractAuditController<T>> controller) {
		super(entityClass, controller);
	}

	@Override
	protected Long getEntityId(T entity) {
		return entity.getId();
	}

	
}
