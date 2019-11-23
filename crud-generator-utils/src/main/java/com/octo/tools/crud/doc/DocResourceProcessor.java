package com.octo.tools.crud.doc;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class DocResourceProcessor implements RepresentationModelProcessor<RepositoryLinksResource> {

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
    	resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ApiDocsController.class).index()).withRel(ApiDocsController._DOC));
        return resource;
    }
}
