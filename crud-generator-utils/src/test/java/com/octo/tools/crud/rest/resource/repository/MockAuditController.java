package com.octo.tools.crud.rest.resource.repository;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.text.ParseException;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;

@RepositoryRestController
@RequestMapping("/history")
public class MockAuditController {

	@RequestMapping(value = "/entityA/search/{id}", method = RequestMethod.GET, 
			consumes = APPLICATION_JSON_VALUE, 
			produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> lastAuditVersion(@PathVariable Long id) throws ParseException, JsonProcessingException {
		
		return new ResponseEntity<String>("{ \"revId\": 2 }", HttpStatus.OK);
	}
	
}
