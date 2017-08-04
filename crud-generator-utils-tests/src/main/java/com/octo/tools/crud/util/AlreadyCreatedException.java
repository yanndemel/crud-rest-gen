package com.octo.tools.crud.util;

public class AlreadyCreatedException extends Exception {

	private String url;

	public AlreadyCreatedException(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
	
	

}
