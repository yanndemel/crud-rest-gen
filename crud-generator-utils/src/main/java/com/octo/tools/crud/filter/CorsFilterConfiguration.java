package com.octo.tools.crud.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.octo.tools.crud.utils.StringUtils;


/**
 * @author OCTO
 * 
 * CORS Filter configuration
 *
 */
@Configuration
public class CorsFilterConfiguration {

	/**
	 * Comma-separated list of allowed origins 
	 * */
	@Value("${http.allowedOrigins:*}")
	private String allowedOrigins;
	
	/**
	 * Comma-separated list of allowed headers 
	 * */
	@Value("${http.allowedHeaders:Cache-Control, Origin, X-Requested-With, Content-Type, Accept, If-Match, Authorization}")
	private String allowedHeaders;
	
	/**
	 * Comma-separated list of allowed methods 
	 * */
	@Value("${http.allowedMethods:POST, GET, PUT, OPTIONS, DELETE, PATCH, HEAD}")
	private String allowedMethods;
	
	/**
	 * Comma-separated list of exposed headers 
	 * */
	@Value("${http.exposedHeaders:Cache-Control, Content-Language, Content-Type, Expires, ETag, Last-Modified, Pragma}")
	private String exposedHeaders;
	
	/**
	 * Max age definition
	 * */
	@Value("${http.maxAge:3600}")
	private Long maxAge;
	
	/**
	 * Allow credentials
	 * */
	@Value("${http.allowCredentials:true}")
	private Boolean allowCredentials;
	
	
	
	@Bean
	public CorsFilter corsFilter() {
	    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    final CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(allowCredentials);
	    config.setAllowedOrigins(StringUtils.toList(allowedOrigins));
	    config.setAllowedHeaders(StringUtils.toList(allowedHeaders));
	    config.setAllowedMethods(StringUtils.toList(allowedMethods));
	    config.setExposedHeaders(StringUtils.toList(exposedHeaders));
	    config.setMaxAge(maxAge);	    
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
	
}
