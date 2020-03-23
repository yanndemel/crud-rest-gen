package com.octo.tools.crud.rest.resource;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ContextConfiguration( classes = TestConfig.class)
public class HttpResourceTest {

	@Autowired
	protected WebApplicationContext context;
	
	protected MockMvc mockMvc;
	@Autowired
	private Environment env;

	
	@Before
	public void init() {		
		mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();	
	}
	
	
	@Test
	public void testSingle() throws Exception {
		String getUrl = String.format("http://localhost:%s/entityAs/1", env.getProperty("server.port"));
		String postUrl = String.format("http://localhost:%s/entityAs", env.getProperty("server.port"));
		mockMvc.perform(post(postUrl).contentType(MediaTypes.HAL_JSON).content("{ \"id\": 1, \"name\": \"Joe\"}")).andExpect(status().isCreated());
		mockMvc.perform(get(getUrl)).andExpect(jsonPath("_links.self.href", Matchers.is(getUrl)));
		String content = "{ \"id\": 1, \"name\": \"Dalton\", \"externalResource\": \"1\"}";
		System.out.println("content = "+content);
		mockMvc.perform(post(String.format("http://localhost:%s/entityBs", env.getProperty("server.port")))
				.contentType(MediaTypes.HAL_JSON)
				.content(content)).andExpect(status().isCreated());
		mockMvc.perform(get("/entityBs/1")).andExpect(jsonPath("jsonValue.deleted", Matchers.is(false)));
		mockMvc.perform(get("/entityBs/1")).andExpect(jsonPath("jsonValue.data.name", Matchers.is("Joe")));
		mockMvc.perform(delete(getUrl)).andExpect(status().isNoContent());
		mockMvc.perform(get("/entityBs/1")).andExpect(jsonPath("jsonValue.deleted", Matchers.is(true)));
		mockMvc.perform(get("/entityBs/1")).andExpect(jsonPath("jsonValue.data.revId", Matchers.is(2)));
		
	}
	
	@Test
	public void testCollection() throws Exception {
		String getUrl = String.format("http://localhost:%s/entityAs/1", env.getProperty("server.port"));
		String postUrl = String.format("http://localhost:%s/entityAs", env.getProperty("server.port"));
		mockMvc.perform(post(postUrl).contentType(MediaTypes.HAL_JSON).content("{ \"id\": 1, \"name\": \"Joe\"}")).andExpect(status().isCreated());
		mockMvc.perform(post(postUrl).contentType(MediaTypes.HAL_JSON).content("{ \"id\": 2, \"name\": \"Jack\"}")).andExpect(status().isCreated());
		mockMvc.perform(get(getUrl)).andExpect(jsonPath("_links.self.href", Matchers.is(getUrl)));
		String content = "{ \"id\": 1, \"name\": \"Dalton\", \"externalResource\": \"1\", "
				+ "\"externalResourceCollection\": [ \"1\", \"2\"] }";
		System.out.println("content = "+content);	
		mockMvc.perform(post(String.format("http://localhost:%s/entityBs", env.getProperty("server.port")))
				.contentType(MediaTypes.HAL_JSON)
				.content(content)).andExpect(status().isCreated());		
		mockMvc.perform(get("/entityBs/1")).andExpect(jsonPath("jsonCollectionValue[1].data.name", Matchers.is("Jack")));
	}
	
	@After
	public void after() {

	}
}
