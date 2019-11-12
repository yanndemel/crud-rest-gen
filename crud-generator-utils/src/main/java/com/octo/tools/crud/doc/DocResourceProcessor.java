package com.octo.tools.crud.doc;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class DocResourceProcessor implements ResourceProcessor<RepositoryLinksResource> {

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
    	resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ApiDocsController.class).index()).withRel(ApiDocsController._DOC));
        return resource;
    }
}
