package com.octo.tools.crud.util;

public class DeleteInfo {
	
	
	public DeleteInfo(Class<?> entityClass, String url) {
		super();
		this.entityClass = entityClass;
		this.url = url;
	}
	
	public Class<?> entityClass;
	public String url;
}
