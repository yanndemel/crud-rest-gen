package com.octo.tools.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RepositoryRestController
public class AuditControllerBase {
	
	@Autowired
	protected EntityManagerFactory emf; 
	
	@RequestMapping(method = RequestMethod.GET, path = com.octo.tools.audit.AbstractAuditController.HISTORY)
	public @ResponseBody ResponseEntity<?> ok() {
		EntityManager em = emf.createEntityManager();
		try {
			Set<AbstractAuditController<?, ?>> registerdcontrollers = AbstractAuditController.getRegisterdcontrollers();
			List<Link> links = new ArrayList<>(registerdcontrollers.size());
			for(AbstractAuditController<?, ?> controller : registerdcontrollers) {
				links.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(controller.getClass()).getRevisions(em)).withRel(controller.getEntityClass().getSimpleName().toLowerCase()));
			}
			return ResponseEntity.ok(new CollectionModel<>(Collections.emptyList(), links));
		} finally {
			em.close();
		}
	}

}
