package com.octo.tools.crud.doc;


import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.octo.tools.audit.AbstractAuditController;
import com.octo.tools.common.AbstractCrudTest;
import com.octo.tools.crud.util.EntityInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BaseApiDocumentation extends AbstractCrudTest {
	
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(ADocEntityGenerator.TARGET_GENERATED_SNIPPETS);
	
		
	@Before
	public void setUp() {
		setDefaultMockMvc(MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build());
	}
	
	@Test
	public void errorExample() throws Exception {
		getMockMvc("", HttpMethod.GET)
				.perform(get("/error")
						.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
						.requestAttr(RequestDispatcher.ERROR_REQUEST_URI,
								"/example")
						.requestAttr(RequestDispatcher.ERROR_MESSAGE,
								"Dummy message"))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("error", Matchers.is("Bad Request")))
				.andExpect(jsonPath("timestamp", Matchers.is(notNullValue())))
				.andExpect(jsonPath("status", Matchers.is(400)))
				.andExpect(jsonPath("path", Matchers.is(notNullValue())))
				.andDo(document("error-example",
						responseFields(
								fieldWithPath("error").description("The HTTP error that occurred, e.g. `Bad Request`"),
								fieldWithPath("message").description("A description of the cause of the error"),
								fieldWithPath("path").description("The path to which the request was made"),
								fieldWithPath("status").description("The HTTP status code, e.g. `400`"),
								fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred"))));
	}

	@Test
	public void indexExample() throws Exception {
		getMockMvc("", HttpMethod.GET).perform(get(""))
			.andExpect(status().isOk())
			.andDo(print()).andDo(document("index-example",
					links(linkResources())));
	}

	private LinkDescriptor[] linkResources() throws ClassNotFoundException {
		List<LinkDescriptor> list = new ArrayList<LinkDescriptor>();
		List<EntityInfo> entityInfoList = getEntityInfoList(em);
		for(EntityInfo entity : entityInfoList) {
			list.add(linkWithRel(entity.getPluralName()).description("The <<resources-"+entity.getPluralName()+","+entity.getSimpleName1stUpper()+" resource>>"));
		}
		list.add(linkWithRel("profile").description("The ALPS profile for the service"));
		if("true".equalsIgnoreCase(System.getProperty("audit")))
			list.add(linkWithRel(AbstractAuditController._HISTORY).description("The <<resources-"+AbstractAuditController._HISTORY+",History resource>>"));
		if("true".equalsIgnoreCase(System.getProperty("doc")))
			list.add(linkWithRel(ApiDocsController._DOC).description("Redirection to the HTML page documenting the API"));
		return list.toArray(new LinkDescriptor[0]);
	}
	
}

	