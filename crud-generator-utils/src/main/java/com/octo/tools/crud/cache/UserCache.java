package com.octo.tools.crud.cache;

import java.util.Calendar;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.octo.tools.crud.utils.HttpRequest;

@Service
@EnableCaching
@CacheConfig(cacheNames = { UserCache.PROFILES, UserCache.AZURE_TOKENS, UserCache.BOX_TOKENS })
public class UserCache {

	private static final Logger logger = LoggerFactory.getLogger(UserCache.class);
	
	private ThreadLocal<String> authKey = new ThreadLocal<>();
	
	public static final String AZURE_TOKENS = "azureTokens";

	public static final String BOX_TOKENS = "boxTokens";

	public static final String PROFILES = "profiles";
	
	@Autowired
	private CacheManager cacheManager;

	public static final String SESSION_TOKEN_KEY = "authToken";
	
	public Profile getCachedUserProfile() {
		String token = getConnectedUserToken();
		return token == null ? null : getCachedUserProfile(token);
	}
	
	private String getConnectedUserToken() {
		String authToken = authKey.get();
		//logger.debug("getConnectedUserToken = "+authToken);
		return authToken;
	}
	
	
	public Profile getCachedUserProfile(String token) {
		Cache cache = cacheManager.getCache(PROFILES);
		ValueWrapper valueWrapper = cache.get(token);
		return valueWrapper != null ? (Profile)valueWrapper.get() : null;
	}
	
	public String getConnectedUserMail() {		
		Profile cachedUserProfile = getCachedUserProfile();
		return (cachedUserProfile == null) ? null : cachedUserProfile.getEmail();
	}

	public void refreshUserProfileInCache(String oldToken, String newToken) throws AuthenticationException {
		Cache cache = cacheManager.getCache(UserCache.PROFILES);	
		ValueWrapper valueWrapper = cache.get(oldToken);
		if(valueWrapper != null) {
			Profile p = (Profile) valueWrapper.get();
			if(p != null) {				
				cache.evict(oldToken);
				p.setAuthToken(newToken);
				cache.put(newToken, p);
				return;
			}
		}
		throw new AuthenticationException("Profile not found in cache...");
	}
	
	public void storeTokenInCache(final OAuth2AccessToken tokens, String sessionId) {
    	storeTokenInCache(tokens, cacheManager.getCache(UserCache.AZURE_TOKENS), sessionId);
    }
    
	private void storeTokenInCache(final OAuth2AccessToken tokens, Cache cache, String sessionId) {
		logger.debug("Storing in cache {}", tokens.getAccessToken());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, tokens.getExpiresIn());
		cache.put(tokens.getAccessToken(), new Token(tokens, cal.getTime(), sessionId));
	}
	
	public void refreshTokenInCache(Token oldToken, OAuth2AccessToken newToken, HttpSession session) throws AuthenticationException {
		Cache cache = cacheManager.getCache(UserCache.AZURE_TOKENS);
		cache.evict(oldToken.getToken().getAccessToken());
		storeTokenInCache(newToken, cache, session.getId());
		refreshUserProfileInCache(oldToken.getToken().getAccessToken(), newToken.getAccessToken());
		session.setAttribute(SESSION_TOKEN_KEY, newToken.getAccessToken());
	}
	
	public void putProfileInCache(String authToken, String name, String userMail) {
		Cache cache = cacheManager.getCache(UserCache.PROFILES);		
		cache.put(authToken, new Profile(name, userMail, authToken));
	}
	
	public Token getCachedAccessToken(String authToken) throws AuthenticationException {
		Cache cache = cacheManager.getCache(UserCache.AZURE_TOKENS);		
		ValueWrapper val = cache.get(authToken);
		Token token = val != null ? (Token) val.get() : null;		
		if(token == null) {
			throw new AuthenticationException("Token is null");
		}
		return token;
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
		return req.authorization(getAuthorizationHeader())
    			.accept(MediaType.APPLICATION_JSON_VALUE)
    			.trustAllCerts()
    			.trustAllHosts();
	}

	
	
}
