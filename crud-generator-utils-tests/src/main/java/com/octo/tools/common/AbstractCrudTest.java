package com.octo.tools.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.tools.crud.doc.ADocEntityGenerator;
import com.octo.tools.crud.util.EntityHelper;
import com.octo.tools.crud.util.EntityInfo;

public abstract class AbstractCrudTest {

	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected EntityManager em;
	@Autowired
	protected WebApplicationContext context;
	protected MockMvc mockMvc;
	protected List<EntityInfo> entityInfoList;
	protected EntityHelper entityHelper;

	@Before
	public void setUp() throws ClassNotFoundException, IOException {
		configureMapper();
		seUpMockMvc();
		setUpEntityList();
	}
	
	@After
	public void after() {
		if(em != null) {
			em.clear();
			em.close();		
		}
	}
	
	private void configureMapper() {
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
	}
	
	protected void initDataSets() throws IOException {
		for (EntityInfo info : entityInfoList) {
			File createDataFile = getCreateDataFile(info);
			if(createDataFile != null && createDataFile.exists())
				info.setDataSet(objectMapper.readValue(createDataFile, new TypeReference<Map<String, String>>() {}));								
			File updateDataFile = getUpdateDataFile(info);
			if(updateDataFile != null && updateDataFile.exists())
				info.setUpdateDataSet(objectMapper.readValue(updateDataFile, new TypeReference<Map<String, String>>() {}));					
		}
	}

	protected void setUpEntityList() throws ClassNotFoundException, IOException {
		this.entityInfoList = ADocEntityGenerator.getEntityInfoList(em);
		initDataSets();
		this.entityHelper = new EntityHelper(mockMvc, objectMapper, entityInfoList);
	}

	protected void seUpMockMvc() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();		
	}

	private File getCreateDataFile(EntityInfo info) {
		String rootPath = getDataFileRootPath(info);
		URL resource = ClassLoader.getSystemResource("./"+rootPath+".json");
		if(resource != null)
			return new File(resource.getFile());
		return null;
	}
	
	private File getUpdateDataFile(EntityInfo info) {
		String rootPath = getDataFileRootPath(info);
		URL resource = ClassLoader.getSystemResource("./"+rootPath+".update.json");
		if(resource != null)
			return new File(resource.getFile());
		return null;
	}

	private String getDataFileRootPath(EntityInfo info) {
		return info.getEntityClass().getPackage().getName().replace(".", "/")+"/"+info.getSimpleName();
	}
		
}