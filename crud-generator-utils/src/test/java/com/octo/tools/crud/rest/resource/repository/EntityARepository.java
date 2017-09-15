package com.octo.tools.crud.rest.resource.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.octo.tools.crud.rest.resource.model.EntityA;

@RepositoryRestResource
public interface EntityARepository extends PagingAndSortingRepository<EntityA, Long> {

}
