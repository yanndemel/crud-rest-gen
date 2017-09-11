package com.octo.tools.crud.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;


/**
 * @author OCTO
 * 
 * CORS Filter to enable all requests origins
 *
 */
@Component
public class SimpleCORSFilter implements Filter {

	//private static final Logger logger = LoggerFactory.getLogger(SimpleCORSFilter.class);
	
	@Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        String origin = request.getHeader("Origin");
        //logger.debug("SimpleCORSFilter : origin = {}", origin);
		response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
        response.setHeader("Vary", "Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH, HEAD");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }        
    }

	@Override
    public void init(FilterConfig filterConfig) {}

	@Override
    public void destroy() {}

}
