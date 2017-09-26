package com.octo.tools.crud.cache;

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
	
	public Profile getCachedUserProfile() {
		String token = getConnectedUserToken();
		return token == null ? null : getCachedUserProfile(token);
	}
	
	private String getConnectedUserToken() {
		String authToken = authKey.get();
		logger.debug("getConnectedUserToken = "+authToken);
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



	public void setAuthToken(String authToken) {
		authKey.set(authToken);		
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
