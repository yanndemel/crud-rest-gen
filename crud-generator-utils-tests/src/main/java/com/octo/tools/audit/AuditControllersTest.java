package com.octo.tools.audit;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.tools.audit.AbstractAuditController;
import com.octo.tools.crud.doc.ADocEntityGenerator;
import com.octo.tools.crud.util.EntityHelper;
import com.octo.tools.crud.util.EntityInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuditControllersTest {

	@Autowired
	protected EntityManager em;
	
	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;
	
	private MockMvc mockMvc;

	private List<EntityInfo> entityInfoList;

	private EntityHelper entityHelper;

	@Before
	public void setUp() throws ClassNotFoundException {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		this.entityInfoList = ADocEntityGenerator.getEntityInfoList(em);
		this.entityHelper = new EntityHelper(mockMvc, objectMapper, entityInfoList);
	}
	
	@Test
	public void testAudit() throws Exception {
		for(EntityInfo info : entityInfoList) {
			Class entityClass = info.getEntityClass();
			entityHelper.createLinkedEntities(entityClass);
			String location = entityHelper.createSampleEntity(info);
			Map<String, String> paramsMap = entityHelper.getParamsMap(entityClass, true);

			this.mockMvc
					.perform(patch(entityHelper.url(location)).contentType(MediaTypes.HAL_JSON)
							.content(this.objectMapper.writeValueAsString(paramsMap)))
					.andExpect(status().isNoContent());
			
			ResultActions result = this.mockMvc.perform(get(getRevisionsUrl(info.getPluralName().toUpperCase()))).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("_embedded.auditResourceSupports[0].revType", Matchers.equalTo("ADD")));		
			Map<String, Object> map = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), Map.class); 
			List list = (List)((Map)map.get("_embedded")).get("auditResourceSupports");
			Object revId = ((Map)list.get(list.size() - 1)).get("revId");
			this.mockMvc.perform(get(getRevisionsForEntityUrl(info.getPluralName().toUpperCase(), location))).andDo(print()).andExpect(jsonPath("_embedded.auditResourceSupports", Matchers.hasSize(2))).andExpect(status().isOk());
			
			this.mockMvc.perform(get(getRevisionEntityUrl(info.getPluralName().toUpperCase(), revId.toString()))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("_embedded.auditResourceSupports[0].revType", Matchers.equalTo("MOD")));
			
			
			entityHelper.deleteLinkedEntities(location);
		}
	}

	private String getRevisionsForEntityUrl(String plural, String location) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		return AbstractAuditController.HISTORY + (String)Class.forName(System.getProperty("packageName")+".URLs").getField(plural + "_SEARCH").get(null) + "?entityId=" + location.substring(location.lastIndexOf("/") + 1);
	}

	private String getRevisionEntityUrl(String plural, String id) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		return AbstractAuditController.HISTORY + (String)Class.forName(System.getProperty("packageName")+".URLs").getField(plural).get(null) + "/" + id;
	}

	private String getRevisionsUrl(String plural) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		return AbstractAuditController.HISTORY + (String)Class.forName(System.getProperty("packageName")+".URLs").getField(plural).get(null);
	}
	
	
	
	
}
