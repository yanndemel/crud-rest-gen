package com.octo.tools.audit;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class AuditResourceProcessor implements ResourceProcessor<RepositoryLinksResource> {

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
    	resource.add(ControllerLinkBuilder.linkTo(AuditControllerBase.class).withRel(AbstractAuditController._HISTORY));
        return resource;
    }
}
