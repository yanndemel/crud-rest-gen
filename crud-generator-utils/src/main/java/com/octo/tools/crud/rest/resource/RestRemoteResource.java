package com.octo.tools.crud.rest.resource;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RestRemoteResource {
	
	private boolean deleted;
	
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	private Map<String, Object> data;
	
	public RestRemoteResource() {
	}

	public RestRemoteResource(boolean deleted, Map<String, Object> data) {
		super();
		this.deleted = deleted;
		this.data = data;
	}

	public RestRemoteResource(Map<String, Object> data) {
		super();
		this.deleted = false;
		this.data = data;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	
	
}
