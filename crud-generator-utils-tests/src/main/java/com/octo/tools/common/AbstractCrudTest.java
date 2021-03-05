package com.octo.tools.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.atteo.evo.inflector.English;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.junit.After;
import org.junit.Before;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.octo.tools.crud.util.EntityInfo;
import com.octo.tools.crud.utils.ReflectionUtils;

@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractCrudTest {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCrudTest.class);
	
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected EntityManagerFactory emf;
	@Autowired
	protected WebApplicationContext context;
	@Autowired
	protected DataSource dataSource;

	private MockMvc mockMvc;
	private Map<String, Map<HttpMethod, MockMvc>> customControllersMockMvc;
	private Map<String, Map<HttpMethod, MockMethod>> customControllersMethods;
	protected List<EntityInfo> entityInfoList;
	protected EntityHelper entityHelper;
	
	private String packageName;
	
	private long sequence;

	public AbstractCrudTest() {
		packageName = System.getProperty("packageName");
		sequence = 0L;
		assert(packageName != null);
	}

	protected Long nextVal() {
		return ++this.sequence;
	}
	
	
	@Before
	public void setUp() throws ClassNotFoundException, IOException {
		configureMapper();
		seUpMockMvc();
		setUpEntityList();	
	}
	
	@After
	public void afterTest() throws ClassNotFoundException, IOException {		
		TableClearer tableClearer = new TableClearer(dataSource);
		tableClearer.clearTables();
	}
	
	
	private void configureMapper() {
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}
	
	protected void initDataSets() throws IOException {
		for (EntityInfo info : entityInfoList) {
			File createDataFile = getCreateDataFile(info);
			if(createDataFile != null && createDataFile.exists())
				info.setDataSet(objectMapper.readValue(createDataFile, new TypeReference<Map<String, Object>>() {}));								
			File updateDataFile = getUpdateDataFile(info);
			if(updateDataFile != null && updateDataFile.exists())
				info.setUpdateDataSet(objectMapper.readValue(updateDataFile, new TypeReference<Map<String, Object>>() {}));					
		}
	}

	protected void setUpEntityList() throws ClassNotFoundException, IOException {
		ExcludedEntities excludedEntities = this.getClass().getAnnotation(ExcludedEntities.class);
		EntityManager em = emf.createEntityManager();
		try {
			this.entityInfoList = getEntityInfoList(em);
			if(excludedEntities != null && excludedEntities.value() != null && excludedEntities.value().length > 0) {
				List<Class<?>> excludedClasses = Arrays.asList(excludedEntities.value());
				this.entityInfoList = this.entityInfoList.stream()
						.filter(info->!excludedClasses.contains(info.getEntityClass())).collect(Collectors.toList());
			}
			initDataSets();
			this.entityHelper = new EntityHelper(this);
		} finally {
			em.close();
		}
	}

	protected void seUpMockMvc() {
		setDefaultMockMvc(MockMvcBuilders.webAppContextSetup(this.context).build());
	}
	
	protected void setDefaultMockMvc(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}
	
	protected void addCustomControllerMethod(String entityClass, HttpMethod httpMethod, MockMethod mockObject) {
		if(this.customControllersMethods == null) {
			this.customControllersMethods = new HashMap<String, Map<HttpMethod, MockMethod>>();
		}
		Map<HttpMethod, MockMethod> map = this.customControllersMethods.get(entityClass);
		if(map == null) {			
			map = new HashMap<>();
			this.customControllersMethods.put(entityClass, map);
		}
		if(!map.containsKey(httpMethod))
			map.put(httpMethod, mockObject);
	}
	
	protected void addCustomController(String entityClass, HttpMethod method, MockMvc mockMvc) {
		if(this.customControllersMockMvc == null)
			this.customControllersMockMvc = new HashMap<>();
		Map<HttpMethod, MockMvc> map = this.customControllersMockMvc.get(entityClass);
		if(map == null) {			
			map = new HashMap<>();
			this.customControllersMockMvc.put(entityClass, map);
		}
		if(!map.containsKey(method))
			map.put(method, mockMvc);
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
	
	protected MockMvc getMockMvc(String entityClass, HttpMethod method) throws MockNotFoundException {
		if(customControllersMockMvc == null)
			return getDefaultMockMvc();
		Map<HttpMethod, MockMvc> map = customControllersMockMvc.get(entityClass);
		if(map != null && map.containsKey(method)) {
			MockMvc mockMvc2 = map.get(method);
			if(mockMvc2 == null) {
				
				throw new MockNotFoundException(String.format("Mock Not Found for entity %s and method %s",  entityClass, method.toString()));
			}
			return mockMvc2;
		}
		return getDefaultMockMvc();
	}
	
	protected MockMethod getMockMethod(String entityClass, HttpMethod httpMethod) {
		if(this.customControllersMethods != null) {
			Map<HttpMethod, MockMethod> map = this.customControllersMethods.get(entityClass);
			if(map != null) {
				return map.get(httpMethod);
			}
		}
		return null;
	}
	
	private MockMvc getDefaultMockMvc() {
		return this.mockMvc;
	}
	
	protected WebApplicationContext getWebContext() {
		return this.context;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public List<EntityInfo> getEntityInfoList() {
		return entityInfoList;
	}
	
	public List<EntityInfo> getEntityInfoList(EntityManager em) {
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		List<EntityInfo> list = new ArrayList<EntityInfo>();
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> allRepos = reflections.getTypesAnnotatedWith(RepositoryRestResource.class);		
		for (EntityType<?> type : entityList) {
			Class<?> javaType = type.getJavaType();
			if (isExposed(javaType) && type.hasSingleIdAttribute()) {
				setDisabledHttpMethodsPerRepo(javaType, allRepos);
				EntityInfo info = new EntityInfo();
				String entity1 = javaType.getSimpleName();
				String entities1 = getPluralName(entity1);
				String entity = getName1stLower(entity1);
				String entities = getName1stLower(entities1);
				info.setEntityClass(javaType);
				info.setSimpleName(entity);
				info.setSimpleName1stUpper(entity1);
				info.setPluralName(entities);
				info.setPluralName1stUpper(entities1);
				Field idField = getIdField(javaType);
				if(idField == null)
					continue;
				info.setIdField(idField.getName());
				info.setIdAuto(idField.isAnnotationPresent(GeneratedValue.class));
				/*if(!info.isIdAuto() && !(idField.getType().equals(Long.class) || idField.getType().equals(long.class))) {
					continue;
				}*/
				try {
					info.setSearch(hasSearch(javaType));
					info.setPaged(isPaged(javaType));
				} catch (ClassNotFoundException e) {
					logger.debug("ClassNotFound", e);
					continue;
				}
				info.setHasOnlyManyToOne(hasOnlyManyToOne(javaType));				
				info.setSingleTableInheritance(ReflectionUtils.isSingleTableInheritance(javaType));
				list.add(info);
			}
		}
		list.forEach(info->{
			if(info.isSingleTableInheritance()) {
				info.setChildEntities(getChildEntities(info, list));
			}
		});
		Collections.sort(list, (p1, p2) -> p1.getSimpleName().compareTo(p2.getSimpleName()));
		return list;
	}

	private Field getIdField(Class<?> javaType) {
		List<Field> allFields = ReflectionUtils.getAllFields(javaType);		
		Field idField = null;
		for(Field f : allFields) {
			if(f.isAnnotationPresent(Id.class)) {
				idField = f;						
				break;
			}
		}
		return idField;
	}

	public boolean isExposed(Class<?> javaType) {
		return ReflectionUtils.isEntityExposed(javaType);
	}

	private List<EntityInfo> getChildEntities(EntityInfo info, List<EntityInfo> list) {
		return list.stream().filter((i)->info.getEntityClass().isAssignableFrom(i.getEntityClass()) 
				&& i.getEntityClass().isAnnotationPresent(DiscriminatorValue.class))
				.collect(Collectors.toList());
	}

	private void setDisabledHttpMethodsPerRepo(Class<?> entityClass, Set<Class<?>> allRepos) {
		Class<?> entityRepo = null;
		for(Class<?> c : allRepos) {
			Type[] genericInterfaces = c.getGenericInterfaces();
			if(genericInterfaces != null && genericInterfaces.length > 0) {
				for(Type type : genericInterfaces) {
					if(type instanceof ParameterizedType) {
						ParameterizedType genericType = (ParameterizedType)type;
						if(genericType.getActualTypeArguments() != null 
							&& genericType.getActualTypeArguments().length == 2
							&& genericType.getActualTypeArguments()[0].getTypeName().equals(entityClass.getName())) {
								entityRepo = c;
								break;
						}
					}
				}
			}
		}
		if(entityRepo != null) {
			Method[] methods = entityRepo.getMethods();
			if(methods != null && methods.length > 0) {
				for(Method m : methods) {
					RestResource restResourceAnn = m.getAnnotation(RestResource.class);
					if(restResourceAnn != null) {
						if(!restResourceAnn.exported()) {
							if("save".equals(m.getName()) && m.getParameterTypes() != null
									&& m.getParameterTypes().length == 1
									&& m.getParameterTypes()[0].equals(entityClass)
									&& m.getReturnType().equals(entityClass)) {
								addCustomController(entityClass.getName(), HttpMethod.PATCH, null);
								addCustomController(entityClass.getName(), HttpMethod.POST, null);
								addCustomController(entityClass.getName(), HttpMethod.PUT, null);
							} else if("delete".equals(m.getName()) 
									&& m.getParameterTypes()!= null
									&& m.getParameterTypes().length == 1)
								addCustomController(entityClass.getName(), HttpMethod.DELETE, null);
							else if("findOne".equals(m.getName()) && m.getParameterTypes() != null
									&& m.getParameterTypes().length == 1								
									&& m.getReturnType().equals(entityClass)) {
								addCustomController(entityClass.getName(), HttpMethod.GET, null);
							}
						}
					}
				}	
			}
		}
	}


	private boolean hasOnlyManyToOne(Class<?> javaType) {
		int total = 0;
		int many = 0;
		for(Field f : ReflectionUtils.getAllFields(javaType)) {
			if(!f.isAnnotationPresent(Id.class) && !f.isAnnotationPresent(Transient.class)
					&& !f.isAnnotationPresent(Version.class)) {
				total++;
				if(f.isAnnotationPresent(ManyToOne.class))
					many++;
			}
		}
		return total == many;
	}

	private boolean isPaged(Class<?> javaType) throws ClassNotFoundException {		
		return PagingAndSortingRepository.class.isAssignableFrom(getRepository(javaType));
	}

	private boolean hasSearch(Class<?> javaType) throws ClassNotFoundException {
		Class<?> repoClass = getRepository(javaType);
		Method[] methods = repoClass.getDeclaredMethods();
		for(Method m : methods) {
			if(m.isAnnotationPresent(RestResource.class)) {
				RestResource ress = m.getAnnotation(RestResource.class);				
				if(ress.exported()) {
					logger.debug("-->hasSearch for {} is TRUE", javaType.getSimpleName());
					return true;
				}
			}
		}
		logger.debug("-->hasSearch for {} is FALSE", javaType.getSimpleName());
		return false;
	}

	private Class<?> getRepository(Class<?> javaType) throws ClassNotFoundException {
		return Class.forName(packageName + "." + javaType.getSimpleName()+"Repository");
	}
	
	public String getName1stLower(String name) {
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	private String getPluralName(String name) {
		return English.plural(name);		
	}
	
}