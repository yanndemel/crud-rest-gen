package com.octo.tools.crud.rest.resource.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.octo.tools.crud.rest.annotation.RestResourceMapper;
import com.octo.tools.crud.rest.resource.RestRemoteResource;

@Entity
public class EntityB {

	@Id
	private Long id;
	
	private String name;
	
	@RestResourceMapper(context="http://localhost:8758", path="/entityAs/#id", 
			external = true, resolveToProperty = "jsonValue",
			lastRevisionPath = "/history/entityA/search/#id")
	private Long externalResource;
	
	@Transient	
	private RestRemoteResource jsonValue;
	
	@ElementCollection
	@RestResourceMapper(context="http://localhost:8758", 
		path="/entityAs/#id", external = true, 
		resolveToProperty = "jsonCollectionValue")
	private List<String> externalResourceCollection;
	
	@Transient
	private List<RestRemoteResource> jsonCollectionValue;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	

	public List<String> getExternalResourceCollection() {
		return externalResourceCollection;
	}

	public void setExternalResourceCollection(List<String> externalResourceCollection) {
		this.externalResourceCollection = externalResourceCollection;
	}

	

	public Long getExternalResource() {
		return externalResource;
	}

	public void setExternalResource(Long externalResource) {
		this.externalResource = externalResource;
	}

	public RestRemoteResource getJsonValue() {
		return jsonValue;
	}

	public void setJsonValue(RestRemoteResource jsonValue) {
		this.jsonValue = jsonValue;
	}

	public List<RestRemoteResource> getJsonCollectionValue() {
		return jsonCollectionValue;
	}

	public void setJsonCollectionValue(List<RestRemoteResource> jsonCollectionValue) {
		this.jsonCollectionValue = jsonCollectionValue;
	}
	
	
	
}
