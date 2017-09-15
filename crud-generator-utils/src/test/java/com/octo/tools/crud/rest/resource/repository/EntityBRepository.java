package com.octo.tools.crud.rest.resource.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.octo.tools.crud.rest.resource.model.EntityB;

@RepositoryRestResource
public interface EntityBRepository extends PagingAndSortingRepository<EntityB, Long> {

}
