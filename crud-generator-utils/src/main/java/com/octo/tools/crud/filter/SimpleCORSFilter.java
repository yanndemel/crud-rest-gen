package com.octo.tools.crud.filter;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.octo.tools.crud.utils.StringUtils;


/**
 * 
 * CORS Filter configuration
 *
 */
@Component
public class SimpleCORSFilter implements Filter {

	private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

	private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

	private static final String ORIGIN = "Origin";

	private static final String VARY = "Vary";

	private static final String OPTIONS = "OPTIONS";

	private static final String ALL_ORIGINS = "*";

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

	private boolean allOrigins;
	private List<String> allowedOriginsList;
	
	@PostConstruct
	public void init() {
		this.allOrigins = ALL_ORIGINS.equals(allowedOrigins);
		if(!allOrigins) {
			this.allowedOriginsList = StringUtils.toList(allowedOrigins);	
		}		
	}
	
	@Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        String origin = request.getHeader(ORIGIN);
		if(origin == null) {
			response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_ORIGINS);	
		} else if(!allOrigins) {
			if(allowedOriginsList.contains(origin)) {
				response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			} else {
				response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access to '"+request.getRequestURL().toString()+"' from origin '"+origin+"' has been blocked by CORS policy.");
				return;
			}
		} else {
			response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_ORIGINS);	
		}
        response.setHeader(VARY, ORIGIN);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentials.toString());
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
        response.setHeader(ACCESS_CONTROL_MAX_AGE, maxAge.toString());
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
        response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders.toString());
        if (OPTIONS.equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }        
    }

	@Override
    public void init(FilterConfig filterConfig) {}

	@Override
    public void destroy() {}
	
	/*@Bean
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
	}*/
	
}
