package com.octo.tools.audit;

import org.hibernate.envers.DefaultRevisionEntity;

public abstract class AbstractDefaultAuditController<T> extends AbstractAuditController<T, DefaultRevisionEntity> {

	
	
    public AbstractDefaultAuditController(Class<T> entityClass, Class<? extends AbstractDefaultAuditController> controller) {
		super(entityClass, controller);
	}

	@Override
	protected Long getRevisionEntityId(DefaultRevisionEntity revEntity) {
		return Long.valueOf(revEntity.getId());
	}

	@Override
	protected Long getRevisionEntityTimestamp(DefaultRevisionEntity revEntity) {
		return revEntity.getTimestamp();
	}

	

	
	
	
}
