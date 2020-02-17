package com.octo.tools.crud.cache;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@Data
public class Profile implements Serializable {

	/* *
	 * Stored in cache
	 * */
	private String displayName;
	private String firstname;
	private String email;	
	private Long userId;
	private Long entityId;
	private Long tenantId;
	
	/**
	 * Sent to client with profile info (not stored in cache)
	 * */
	private String token;
	private String refreshToken;
	private long expiresIn;
	
	//Set to true for internal users
	private boolean internal;
	
	public Profile(String token, String displayName, String email, Long userId, String firstName, Long entityId, Long tenantId, boolean internal) {
		super();
		this.token = token;
		this.displayName = displayName;
		this.firstname = firstName;
		this.email = email;
		this.userId = userId;
		this.entityId = entityId;
		this.tenantId = tenantId;
		this.internal = internal;
	}

	/*Use to send RefreshToken infos*/
	public Profile(String displayName, String email, String token, String refreshToken, long expiresIn) {
		super();
		this.displayName = displayName;
		this.email = email;
		this.token = token;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
		this.internal = true;
	}
	
	
}
