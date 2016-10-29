package com.octo.tools.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;

public abstract class AbstractAuditController<T, R> {

	@Autowired
	protected EntityManager em;
		
	protected final Class<T> entityClass;
	protected final Class<? extends AbstractAuditController> controllerClass;


	public static final String _HISTORY = "history";
	public static final String HISTORY = "/" + _HISTORY;
	
	protected static final Set<AbstractAuditController> registerdControllers = new HashSet<>();
	
    public AbstractAuditController(Class<T> entityClass, Class<? extends AbstractAuditController> controller) {
		super();
		this.entityClass = entityClass;
		this.controllerClass = controller;		
		registerdControllers.add(this);
	}


	public ResponseEntity<?> getRevisionsForEntity(
    			Long id) {
		AuditQueryCreator auditQueryCreator = getAuditQueryCreator();
		List<Object[]> resultList = auditQueryCreator.forRevisionsOfEntity(entityClass, false, true).add(AuditEntity.id().eq(id)).getResultList();
		Resources<AuditResourceSupport<T>> resources = getAuditInfoList(resultList);
		return ResponseEntity.ok(resources);
	}


	private Resources<AuditResourceSupport<T>> getAuditInfoList(List<Object[]> resultList) {		
		int size = resultList != null ? resultList.size() : 0;
		if(size == 0)
			return new Resources<>(Collections.emptyList());
		List<AuditResourceSupport<T>> auditInfoList = new ArrayList<>(size);
		List<Link> links = new ArrayList<>(size); 
		for(Object[] revData : resultList) {
			T entity = (T)revData[0];
			R revEntity = (R)revData[1];
			revEntity = unproxy(revEntity); 
			AuditResourceSupport<T> auditResourceSupport = newAuditResourceSupport((RevisionType)revData[2], entity, revEntity);
			auditResourceSupport.add(newSelfLink(getRevisionEntityId(revEntity)));
			auditInfoList.add(auditResourceSupport);
		}
		return new Resources<>(auditInfoList, links);
	}

	public static <T> T unproxy(T entity) {
	    
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
	                .getImplementation();
	    }
	    return entity;
	}

	protected AuditResourceSupport<T> newAuditResourceSupport(RevisionType revType, T entity, R revEntity) {
		return new AuditResourceSupport<T>(entity, getEntityId(entity), getRevisionEntityId(revEntity), getRevisionEntityTimestamp(revEntity), revType);
	}

	protected abstract Long getRevisionEntityId(R revEntity);


	protected abstract Long getRevisionEntityTimestamp(R revEntity);


	protected abstract Long getEntityId(T entity);

	
    public ResponseEntity<?> getRevisions() {
		AuditQueryCreator auditQueryCreator = getAuditQueryCreator();
		List<Object[]> resultList = auditQueryCreator.forRevisionsOfEntity(entityClass, false, true)
				    	.getResultList();
		return ResponseEntity.ok(getAuditInfoList(resultList));
		
	}

	private AuditQueryCreator getAuditQueryCreator() {
		return  getAuditReader().createQuery();
	}


	private AuditReader getAuditReader() {
		Session session = (Session)em.unwrap(Session.class);
		return AuditReaderFactory.get(session);
	}


	public ResponseEntity<?> getRevisionEntity(Long revId) {
		List<Object[]> resultList = getAuditQueryCreator().forRevisionsOfEntity(entityClass, false, true).add(AuditEntity.revisionNumber().eq(revId)).getResultList();
		return ResponseEntity.ok(getAuditInfoList(resultList));
	}
 
	private Link newSelfLink(Long revId) {
		return ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(controllerClass).getRevisionEntity(revId)).withSelfRel();
	}
	

	public static Set<AbstractAuditController> getRegisterdcontrollers() {
		return registerdControllers;
	}


	public Class<T> getEntityClass() {
		return entityClass;
	}
	
	
	
}
