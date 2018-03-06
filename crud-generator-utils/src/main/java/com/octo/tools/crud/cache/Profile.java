package com.octo.tools.crud.cache;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Profile implements Serializable {

	/* *
	 * Stored in cache
	 * */
	private String displayName;
	private String email;	
	
	/**
	 * Sent to client with profile info (not stored in cache)
	 * */
	private String token;
	private String refreshToken;
	private long expiresIn;
	
	
	public Profile(String displayName, String email) {
		super();
		this.displayName = displayName;
		this.email = email;
	}

	/*Use to send RefreshToken infos*/
	public Profile(String token, String refreshToken, long expiresIn) {
		super();
		this.token = token;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
	}
	
	
}
