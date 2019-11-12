package com.octo.tools.audit;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class AuditResourceProcessor implements RepresentationModelProcessor<RepositoryLinksResource> {

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
    	resource.add(WebMvcLinkBuilder.linkTo(AuditControllerBase.class).withRel(AbstractAuditController._HISTORY));
        return resource;
    }
}
