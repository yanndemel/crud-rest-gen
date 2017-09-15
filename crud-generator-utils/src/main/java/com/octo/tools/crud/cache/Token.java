package com.octo.tools.crud.cache;

import java.io.Serializable;
import java.util.Date;

import com.github.scribejava.core.model.OAuth2AccessToken;

public class Token implements Serializable {

	private OAuth2AccessToken token;
	
	private Date expirationDate;

	public Token() {
	}

	public Token(OAuth2AccessToken token, Date expirationDate) {
		this.token = token;
		this.expirationDate = expirationDate;
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
	
	
	
}
