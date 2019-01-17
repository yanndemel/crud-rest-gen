package com.octo.tools.crud.cache;

import java.io.Serializable;

import com.github.scribejava.core.model.OAuth2AccessToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token implements Serializable {

	private OAuth2AccessToken token;
	private String sessionId;
	private long creationTime;
	
	public boolean isExpired() {
		if(token.getExpiresIn() != null)
			return System.currentTimeMillis() >= creationTime + token.getExpiresIn() * 1000L;
		return false;
	}
	
}
