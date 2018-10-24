package com.octo.tools.crud.cache;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
@Data
public class Profile implements Serializable {

	/* *
	 * Stored in cache
	 * */
	private String displayName;
	private String email;	
	private Long userId;
	
	/**
	 * Sent to client with profile info (not stored in cache)
	 * */
	private String token;
	private String refreshToken;
	private long expiresIn;
	
	
	public Profile(String displayName, String email, Long userId) {
		super();
		this.displayName = displayName;
		this.email = email;
		this.userId = userId;
	}

	/*Use to send RefreshToken infos*/
	public Profile(String displayName, String email, String token, String refreshToken, long expiresIn) {
		super();
		this.displayName = displayName;
		this.email = email;
		this.token = token;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
	}
	
	
}
