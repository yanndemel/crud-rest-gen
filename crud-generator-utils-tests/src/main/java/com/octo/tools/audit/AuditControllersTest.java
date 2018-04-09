package com.octo.tools.audit;

import static org.junit.Assert.assertEquals;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.hibernate.envers.Audited;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.octo.tools.common.AbstractCrudTest;
import com.octo.tools.common.MockNotFoundException;
import com.octo.tools.crud.util.EntityInfo;
import com.octo.tools.crud.utils.ReflectionUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.properties.org.hibernate.envers.store_data_at_delete=true")
public class AuditControllersTest extends AbstractCrudTest {
	
	private static final Logger logger = LoggerFactory.getLogger(AuditControllersTest.class);
	
	
	
	@Test
	public void testAudit() throws Exception {
		for(EntityInfo info : entityInfoList) {
			Class entityClass = info.getEntityClass();
			if(isAudited(entityClass)) {
				try {
					performAuditTest(info, entityClass);
				} catch (MockNotFoundException e) {
					logger.debug("Disabling audit documentation for entity "+info.getEntityClass().getName(), e);
					entityHelper.clearLinkedEntities();
					entityHelper.reset();
				}
			}
		}
	}

	public boolean isAudited(Class entityClass) {
		return entityClass != null && entityClass.isAnnotationPresent(Audited.class) && ReflectionUtils.isEntityExposed(entityClass);
	}
	
	public boolean isExposed(Class javaType) {
		return isAudited(javaType);
	}

	private void performAuditTest(EntityInfo info, Class entityClass) throws Exception, JsonProcessingException,
			MockNotFoundException, UnsupportedEncodingException, IllegalAccessException, NoSuchFieldException,
			ClassNotFoundException, IOException, JsonParseException, JsonMappingException {
		entityHelper.createLinkedEntities(entityClass);
		String location = entityHelper.createSampleEntity(info);
		if(location != null) {
			MockMvc getMockMvc = getMockMvc(entityClass.getName(), HttpMethod.GET);
			logger.debug("Created entity : "+getMockMvc.perform(get(location)).andReturn().getResponse().getContentAsString());
			Map<String, Object> paramsMap = entityHelper.getParamsMap(entityClass, true);
			logger.debug("Updating "+location+" with params "+paramsMap);
			getMockMvc(entityClass.getName(), HttpMethod.PATCH)
					.perform(patch(entityHelper.url(location)).contentType(MediaTypes.HAL_JSON)
							.content(this.objectMapper.writeValueAsString(paramsMap)))
					.andExpect(status().isNoContent());
			logger.debug("Updated entity : "+getMockMvc.perform(get(location)).andReturn().getResponse().getContentAsString());
			ResultActions result = getMockMvc.perform(get(getRevisionsUrl(info.getPluralName().toUpperCase()))).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("_embedded.auditResourceSupports[0].revType", Matchers.equalTo("ADD")));		
			Map<String, Object> map = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), Map.class); 
			List<Map<String, Object>> list = (List)((Map)map.get("_embedded")).get("auditResourceSupports");
			Object entity = list.get(list.size() - 1).get("entity");
			Object revId = ((Map)list.get(list.size() - 1)).get("revId");
			Integer entityId = Integer.parseInt(location.substring(location.lastIndexOf("/") + 1));
			getMockMvc.perform(get(getRevisionsForEntityUrl(info.getPluralName().toUpperCase(), location))).andDo(print()).andExpect(jsonPath("_embedded.auditResourceSupports", Matchers.hasSize(2))).andExpect(status().isOk());
			
			getMockMvc.perform(get(getRevisionEntityUrl(info.getPluralName().toUpperCase(), revId.toString()))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("_embedded.auditResourceSupports[0].revType", Matchers.equalTo("MOD")));
			
			entityHelper.deleteLinkedEntities(location, entityClass.getName());
			
			ResultActions res = getMockMvc.perform(get(getLastRevisionForDeletedEntity(info.getPluralName().toUpperCase(), location))).andDo(print()).andExpect(jsonPath("entityId", Matchers.equalTo(entityId))).andExpect(status().isOk());
			Map<String, Object> mapDel = objectMapper.readValue(res.andReturn().getResponse().getContentAsString(), Map.class); 
			assertEquals(objectMapper.writeValueAsString(entity), objectMapper.writeValueAsString(mapDel.get("entity")));
			entityHelper.reset();
		}
		
	}
	
	private String getLastRevisionForDeletedEntity(String plural, String location) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		return AbstractAuditController.HISTORY + (String)Class.forName(System.getProperty("packageName")+".URLs").getField(plural + "_SEARCH").get(null) + "?entityId=" + location.substring(location.lastIndexOf("/") + 1) + "&lastDelRev=true";
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
