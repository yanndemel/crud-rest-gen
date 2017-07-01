package com.octo.tools.audit;

import java.util.Date;
import java.util.List;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.http.ResponseEntity;

public abstract class AbstractDefaultAuditController<T> extends AbstractAuditController<T, DefaultRevisionEntity> {

	
	
    public AbstractDefaultAuditController(Class<T> entityClass, Class<? extends AbstractDefaultAuditController> controller) {
		super(entityClass, controller);
	}

	@Override
	protected Long getRevisionEntityId(DefaultRevisionEntity revEntity) {
		return Long.valueOf(revEntity.getId());
	}

	@Override
	protected Date getRevisionEntityTimestamp(DefaultRevisionEntity revEntity) {
		return new Date(revEntity.getTimestamp());
	}

	@Override
	public ResponseEntity<?> getRevisionEntity(Long revId) {
		List<Object[]> resultList = getAuditQueryCreator().forRevisionsOfEntity(entityClass, false, true).add(AuditEntity.revisionNumber().eq(revId.intValue())).getResultList();
		return ResponseEntity.ok(getAuditInfoList(resultList));
	}
	
	
	
}
