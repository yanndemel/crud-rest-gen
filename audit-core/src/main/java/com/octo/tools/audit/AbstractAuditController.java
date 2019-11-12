package com.octo.tools.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;

public abstract class AbstractAuditController<T, R> {

	@Autowired
	protected EntityManagerFactory emf; 
	
	protected final Class<T> entityClass;
	protected final Class<? extends AbstractAuditController<T, R>> controllerClass;


	public static final String _HISTORY = "history";
	public static final String HISTORY = "/" + _HISTORY;
	
	protected static final Set<AbstractAuditController<?,?>> registerdControllers = new HashSet<>();
	
    public AbstractAuditController(Class<T> entityClass, Class<? extends AbstractAuditController<T, R>> controller) {
		super();
		this.entityClass = entityClass;
		this.controllerClass = controller;		
		registerdControllers.add(this);
	}


    @SuppressWarnings("unchecked")
	protected ResponseEntity<?> getRevisionsForEntity(Long entityId, EntityManager em) {
		AuditQueryCreator auditQueryCreator = getAuditQueryCreator(em);
		List<Object[]> resultList = auditQueryCreator.forRevisionsOfEntity(entityClass, false, true).add(AuditEntity.id().eq(entityId)).getResultList();
		CollectionModel<AuditResourceSupport<T>> resources = getAuditInfoList(resultList, em);
		return ResponseEntity.ok(resources);
	}
	
	protected ResponseEntity<?> getLastRevisionForDeletedEntity(Long entityId, EntityManager em) {		
		AuditQueryCreator auditQueryCreator = getAuditQueryCreator(em);
		try {
			Object[] revData = (Object[]) auditQueryCreator.forRevisionsOfEntity(entityClass, false, true)
					.add(AuditEntity.id().eq(entityId))
					.add(AuditEntity.revisionType().eq(RevisionType.DEL))
					.getSingleResult();
			if(revData == null)
				return ResponseEntity.notFound().build();
			AuditResourceSupport<T> auditInfo = getAuditInfo(revData, em);
			return ResponseEntity.ok(new EntityModel<>(auditInfo));
		} catch (NoResultException e) {
			return ResponseEntity.notFound().build();
		}
	}


	protected CollectionModel<AuditResourceSupport<T>> getAuditInfoList(List<Object[]> resultList, EntityManager em) {		
		int size = resultList != null ? resultList.size() : 0;
		if(size == 0)
			return new CollectionModel<>(Collections.emptyList());
		List<AuditResourceSupport<T>> auditInfoList = new ArrayList<>(size);
		List<Link> links = new ArrayList<>(size); 
		for(Object[] revData : resultList) {
			AuditResourceSupport<T> auditResourceSupport = getAuditInfo(revData, em);
			auditInfoList.add(auditResourceSupport);
		}
		return new CollectionModel<>(auditInfoList, links);
	}


	@SuppressWarnings("unchecked")
	private AuditResourceSupport<T> getAuditInfo(Object[] revData, EntityManager em) {
		T entity = (T)revData[0];
		R revEntity = (R)revData[1];
		revEntity = unproxy(revEntity); 
		AuditResourceSupport<T> auditResourceSupport = newAuditResourceSupport((RevisionType)revData[2], entity, revEntity);
		auditResourceSupport.add(newSelfLink(getRevisionEntityId(revEntity), em));
		return auditResourceSupport;
	}

	@SuppressWarnings("unchecked")
	private static <T> T unproxy(T entity) {
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


	protected abstract Date getRevisionEntityTimestamp(R revEntity);


	protected abstract Long getEntityId(T entity);

	
    @SuppressWarnings("unchecked")
	public ResponseEntity<?> getRevisions(EntityManager em) {
		AuditQueryCreator auditQueryCreator = getAuditQueryCreator(em);
		List<Object[]> resultList = auditQueryCreator.forRevisionsOfEntity(entityClass, false, true)
				    	.getResultList();
		return ResponseEntity.ok(getAuditInfoList(resultList, em));
		
	}

	protected AuditQueryCreator getAuditQueryCreator(EntityManager em) {
		return  getAuditReader(em).createQuery();
	}


	private AuditReader getAuditReader(EntityManager em) {
		Session session = (Session)em.unwrap(Session.class);
		return AuditReaderFactory.get(session);
	}


	@SuppressWarnings("unchecked")
	public ResponseEntity<?> getRevisionEntity(Long revId, EntityManager em) {
		List<Object[]> resultList = getAuditQueryCreator(em).forRevisionsOfEntity(entityClass, false, true).add(AuditEntity.revisionNumber().eq(revId)).getResultList();
		return ResponseEntity.ok(getAuditInfoList(resultList, em));
	}
 
	private Link newSelfLink(Long revId, EntityManager em) {
		return WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(controllerClass).getRevisionEntity(revId, em)).withSelfRel();
	}
	

	public static Set<AbstractAuditController<?, ?>> getRegisterdcontrollers() {
		return registerdControllers;
	}


	public Class<T> getEntityClass() {
		return entityClass;
	}
	
	
	
}
