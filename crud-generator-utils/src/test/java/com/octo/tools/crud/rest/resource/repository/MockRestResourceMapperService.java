package com.octo.tools.crud.rest.resource.repository;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.octo.tools.crud.rest.resource.HttpRequestException;
import com.octo.tools.crud.rest.resource.RestResourceMapperService;

@Service
public class MockRestResourceMapperService extends RestResourceMapperService {

	@Autowired
	private MockAuditController auditController;
	
	protected MockMvc mockMvc;
	
	@Autowired
	public MockRestResourceMapperService() {
		super();
	}

	@PostConstruct
	public void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(auditController).build();
	}
	
	@Override
	protected String getLastEntityRevision(String restResourceURL)
			throws JsonParseException, JsonMappingException, IOException, HttpRequestException {
		try {
			return mockMvc.perform(get(restResourceURL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}			
	}

	
	
}
