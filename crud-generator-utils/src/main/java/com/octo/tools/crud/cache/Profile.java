package com.octo.tools.crud.cache;

import java.io.Serializable;

public class Profile implements Serializable {

	private String displayName;
	private String email;
	private String authToken;
	
	public Profile() {
	}	
	
	public Profile(String displayName, String email, String authToken) {
		super();
		this.displayName = displayName;
		this.email = email;
		this.authToken = authToken;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String json) {
		this.authToken = json;
	}
	
	
	
}
