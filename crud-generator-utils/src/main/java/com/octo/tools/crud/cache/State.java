package com.octo.tools.crud.cache;

import java.io.Serializable;

public class State implements Serializable {

	private String authorizationUrl;
	private String state;
	private String fromURI;

	public State() {
	}
	
	public State(String authorizationUrl, String state, String fromURI) {
		this.authorizationUrl = authorizationUrl;
		this.state = state;
		this.fromURI = fromURI;
	}
	public String getAuthorizationUrl() {
		return authorizationUrl;
	}
	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getFromURI() {
		return fromURI;
	}
	public void setFromURI(String fromURI) {
		this.fromURI = fromURI;
	}
	
	
}
