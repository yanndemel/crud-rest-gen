package com.octo.tools.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RepositoryRestController
@RequestMapping(AbstractAuditController.HISTORY)
public class AuditControllerBase {
	
	@RequestMapping(method = RequestMethod.GET) 
	public @ResponseBody ResponseEntity<?> ok() {
		Set<AbstractAuditController<?, ?>> registerdcontrollers = AbstractAuditController.getRegisterdcontrollers();
		List<Link> links = new ArrayList<>(registerdcontrollers.size());
		for(AbstractAuditController<?, ?> controller : registerdcontrollers) {
			links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(controller.getClass()).getRevisions()).withRel(controller.getEntityClass().getSimpleName().toLowerCase()));
		}
		return ResponseEntity.ok(new Resources<>(Collections.emptyList(), links));
	}

}
