package com.octo.tools.crud.rest.resource;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.tools.crud.cache.UserCache;
import com.octo.tools.crud.rest.annotation.RestResourceMapper;
import com.octo.tools.crud.utils.HttpRequest;

@Service
public class RestResourceMapperService {
		    
    private static final Logger logger = LoggerFactory.getLogger(RestResourceMapperService.class);
    
    @Autowired
    private Environment env;
    
	@Autowired
    private EntityLinks entityLinks;
	
	@Autowired
	private UserCache userCache;
	
	private ObjectMapper mapper;
	
	@PostConstruct
	public void init() {
		mapper = new ObjectMapper();
	}

    public String getResourceURL(final RestResourceMapper restResourceMapper, final Object resourceId) {
        String path = restResourceMapper.path().replaceAll(RestResourceMapper.RESOURCE_ID_PLACEHOLDER, resourceId.toString());
        String restResourceUrl = getContext(restResourceMapper.context()) + path;
        return restResourceUrl.replaceAll(RestResourceMapper.RESOURCE_ID_PLACEHOLDER, resourceId.toString());
    }
    
    public String getLastRevisionResourceURL(final RestResourceMapper restResourceMapper, final Object resourceId) {
    	if(restResourceMapper.lastRevisionPath().isEmpty()) {
    		return null;
    	}
        String path = restResourceMapper.lastRevisionPath().replaceAll(RestResourceMapper.RESOURCE_ID_PLACEHOLDER, resourceId.toString());
        String restResourceUrl = getContext(restResourceMapper.auditContext().isEmpty() ? restResourceMapper.context() : restResourceMapper.auditContext()) + path;
        return restResourceUrl.replaceAll(RestResourceMapper.RESOURCE_ID_PLACEHOLDER, resourceId.toString());
    }
	private String getContext(String context) {
		return context.startsWith("${") ? env.getProperty(context.substring(2, context.length() - 1)) : context;
	}
    
    public static String paramsToQueryString(final String[] params) {
        if (params == null || params.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(String p : params) {
        	sb.append(p);
            if (i < params.length - 1) {
                sb.append("&");
            }
            i++;
        }
        return sb.toString();
    }
    

    public String getHATEOASURLForResource(final String restResourceURL, final Class entityClass) throws MalformedURLException {
        URL resourceURL = new URL(restResourceURL);
        //use HATEOAS LinkBuilder to get the right host and port for constructing the appropriate resource link
        LinkBuilder linkBuilder = entityLinks.linkFor(entityClass);
        URL hateoasURL = new URL(linkBuilder.withSelfRel().getHref());
        
        String query = resourceURL.getQuery();
        if(query != null)
        	resourceURL = new URL(String.format("%s://%s:%o%s?%s", hateoasURL.getProtocol(), hateoasURL.getHost(), hateoasURL.getPort(), resourceURL.getPath(), query));
        else
        	resourceURL = new URL(String.format("%s://%s:%o%s", hateoasURL.getProtocol(), hateoasURL.getHost(), hateoasURL.getPort(), resourceURL.getPath()));
        		
        return resourceURL.toString();
    }    
    
    public RestRemoteResource getResolvedResource(String restResourceURL, RestResourceMapper annotation, Object resourceObjectId) throws HttpRequestException, JsonParseException, JsonMappingException, IOException {
    	HttpRequest req = userCache.setHeaders(HttpRequest.get(restResourceURL));
    	try {
			if(req.ok()) {
				return new RestRemoteResource(mapToJson(req.body()));		
			} else if(req.notFound()) {
				logger.warn("The entity {} may have been deleted -> getting it from history", restResourceURL);
				//The entity may have been deleted -> getting it from history
				restResourceURL = getLastRevisionResourceURL(annotation, resourceObjectId);
				if(restResourceURL != null) {
					String lastEntityRevision = getLastEntityRevision(restResourceURL);
					if(lastEntityRevision != null)
						return new RestRemoteResource(true, mapToJson(lastEntityRevision));	
				}				
			}			
		} catch (HttpRequest.HttpRequestException e) {
			throw new HttpRequestException(e.getMessage(), restResourceURL, req.code(), req.body(), e);
		}
    	throw new HttpRequestException("Error resolving URL", restResourceURL, req.code(), req.body());     	    
    }

	private Map<String, Object> mapToJson(String json)
			throws IOException, JsonParseException, JsonMappingException {
		return mapper.readValue(json, new TypeReference<HashMap<String, Object>>() {});
	}

	protected String getLastEntityRevision(String restResourceURL)
			throws JsonParseException, JsonMappingException, IOException, HttpRequestException {
		HttpRequest req = userCache.setHeaders(HttpRequest.get(restResourceURL));
		if(req.ok()) {
			logger.warn("The entity {} has been deleted -> returning last revision", restResourceURL); 
			return req.body();
		}
		throw new HttpRequestException("Error while fetching last revision from history", restResourceURL, req.code(), req.body());     
	}

}
