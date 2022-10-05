package com.octo.tools.crud.cache;

import javax.naming.AuthenticationException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.octo.tools.crud.utils.HttpRequest;
import com.octo.tools.crud.web.MediaType;

import java.util.List;

@Service
@Slf4j
public class UserCache implements IUserCache {

	
	private ThreadLocal<String> authKey = new ThreadLocal<>();
	
	@Autowired
	private HazelcastInstance hazelcast;

	
	public Profile getCachedUserProfile() {
		String token = getConnectedUserToken();
		return token == null ? null : getCachedUserProfile(token);
	}
	
	public String getConnectedUserToken() {
		String authToken = authKey.get();
		//logger.debug("getConnectedUserToken = "+authToken);
		return authToken;
	}
	
	
	public Profile getCachedUserProfile(String token) {
		IMap<Object, Profile> profiles = hazelcast.getMap(PROFILES);
		return profiles.get(token);
	}
	
	public String getConnectedUserMail() {		
		Profile cachedUserProfile = getCachedUserProfile();
		return (cachedUserProfile == null) ? null : cachedUserProfile.getEmail();
	}

	public void refreshUserProfileInCache(String oldToken, String newToken) throws AuthenticationException {
		IMap<Object, Profile> profiles = hazelcast.getMap(PROFILES);
		Profile p = profiles.get(oldToken);
		if(p != null) {				
			profiles.evict(oldToken);
			p.setToken(newToken);
			profiles.put(newToken, p);
			return;
		}
		throw new AuthenticationException("Profile not found in cache...");
	}
	
	public void storeTokenInCache(final OAuth2AccessToken tokens) {
    	storeTokenInCache(tokens, hazelcast.getMap(UserCache.AZURE_TOKENS));
    }
    
	private void storeTokenInCache(final OAuth2AccessToken tokens, IMap<String, Token> cache) {
		//logger.debug("Storing in cache {}", tokens.getAccessToken());
		cache.put(tokens.getAccessToken(), new Token(tokens, System.currentTimeMillis()));
	}
	
	public void refreshTokenInCache(Token oldToken, OAuth2AccessToken newToken) throws AuthenticationException {
		IMap<String, Token> tokens = hazelcast.getMap(UserCache.AZURE_TOKENS);
		tokens.evict(oldToken.getToken().getAccessToken());
		storeTokenInCache(newToken, tokens);
		refreshUserProfileInCache(oldToken.getToken().getAccessToken(), newToken.getAccessToken());
	}	
	
	public Profile putProfileInCache(OAuth2AccessToken authToken, String name, String userMail, Long userId, Long webId, String firstName, Long entityId,
			Long tenantId, boolean internal, List<Long> accountContactIds) {
		IMap<Object, Profile> profiles = hazelcast.getMap(PROFILES);
		Profile profile = new Profile(authToken.getAccessToken(), name, userMail, userId, webId, firstName, entityId, tenantId, internal, accountContactIds);
		profiles.put(authToken.getAccessToken(), profile);
		storeTokenInCache(authToken);
		return profile;
	}
	
	public void putProfileInCache(String accessToken, Profile profile) {
		IMap<Object, Profile> profiles = hazelcast.getMap(PROFILES);	
		profiles.put(accessToken, profile);
	}
	
	public void putProfileInCache(OAuth2AccessToken authToken, String name, String userMail) {
		putProfileInCache(authToken, name, userMail, null, null, null, null, null, true, null);
	}
	
	public void putProfileInCache(OAuth2AccessToken authToken, String name, String userMail, Long userId, Long entityId, Long tenantId) {
		putProfileInCache(authToken, name, userMail, userId, null, null, entityId, tenantId, true, null);
	}
	
	public void putProfileInCache(OAuth2AccessToken authToken, String name, String userMail, Long userId, Long webId, Long entityId, Long tenantId, boolean internal, List<Long> accountContactIds) {
		putProfileInCache(authToken, name, userMail, userId, webId, null, entityId, tenantId, internal, accountContactIds);
	}
	
	public Token getCachedAccessToken(String authToken) throws AuthenticationException {
		IMap<String, Token> tokens = hazelcast.getMap(UserCache.AZURE_TOKENS);
		Token token = tokens.get(authToken);
		if(token == null) {
			throw new AuthenticationException("Token is null");
		}
		return token;
	}
	
	public Token getCachedAccessToken() throws AuthenticationException {		
		return getCachedAccessToken(getConnectedUserToken());
	}

	public void setAuthToken(String authToken) {
		authKey.set(authToken);		
	}
	
	public void removeAuthToken() {
		authKey.remove();		
	}

	public String getAuthorizationHeader() {
		return "Bearer "+getConnectedUserToken();
	}

	public HttpRequest setHeaders(HttpRequest req) {
		return setHeaders(req, MediaType.APPLICATION_JSON);
	}
	
	public HttpRequest setHeaders(HttpRequest req, MediaType mediaType) {
		return req.authorization(getAuthorizationHeader())
    			.accept(mediaType.toString())
    			.trustAllCerts()
    			.trustAllHosts();
	}

	public void evictAuthTokenFromCache(String accessToken) {
		IMap<String, Token> tokens = hazelcast.getMap(UserCache.AZURE_TOKENS);
		tokens.evict(accessToken);
		IMap<Object, Profile> profiles = hazelcast.getMap(PROFILES);
		profiles.evict(accessToken);
		//logger.debug("Access token evicted from caches and session has been invalidated");
	}

	
	
}
