package com.octo.tools.crud.rest.resource.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

@Entity
@Audited
public class EntityA {

	@Id
	private Long id;
	
	private String name;	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
	
	
	
}
