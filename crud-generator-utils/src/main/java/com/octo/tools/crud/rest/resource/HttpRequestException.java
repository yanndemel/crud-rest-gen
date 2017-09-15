package com.octo.tools.crud.rest.resource;

public class HttpRequestException extends Exception {

	private String message;
	private String requestURL;
	private int code;
	private String body;
	
	public HttpRequestException(String message, String requestURL, int code, String body, Exception e) {
		super(e);
		init(message, requestURL, code, body);		
	}

	private void init(String message, String requestURL, int code, String body) {
		if(message != null)
			this.message = message;
		this.requestURL = requestURL;
		this.code = code;
		this.body = body;
	}

	public HttpRequestException(String message, String requestURL, int code, String body) {
		super(message);
		init(null , requestURL, code, body);	
	}

	@Override
	public String getMessage() {		
		return String.format("%s\tRequest URL : %s\tCode : %o\tResponse : %s", message, requestURL, code, body);
	}
	
	

}
