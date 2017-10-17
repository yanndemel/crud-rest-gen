package com.octo.tools.crud.cache;

import java.io.Serializable;
import java.util.Date;

import com.github.scribejava.core.model.OAuth2AccessToken;

public class Token implements Serializable {

	private OAuth2AccessToken token;
	
	private Date expirationDate;
	
	private String sessionId;

	public Token() {
	}


	public Token(OAuth2AccessToken token, Date expirationDate, String sessionId) {
		super();
		this.token = token;
		this.expirationDate = expirationDate;
		this.sessionId = sessionId;
	}


	public OAuth2AccessToken getToken() {
		return token;
	}

	public void setToken(OAuth2AccessToken token) {
		this.token = token;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}


	public String getSessionId() {
		return sessionId;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	
	
}
