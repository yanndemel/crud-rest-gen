package com.octo.tools.common;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.tools.crud.rest.annotation.RestResourceMapper;
import com.octo.tools.crud.rest.resource.RestRemoteResource;
import com.octo.tools.crud.rest.resource.util.RestResourceUtils;
import com.octo.tools.crud.util.AlreadyCreatedException;
import com.octo.tools.crud.util.DeleteInfo;
import com.octo.tools.crud.util.EntityInfo;
import com.octo.tools.crud.util.FieldInfo;
import com.octo.tools.crud.utils.ReflectionUtils;

public class EntityHelper {

	private static final Logger logger = LoggerFactory.getLogger(EntityHelper.class);
	
	private static final String EXAMPLE_MAIL = "example@gmail.com";
	
	private List<DeleteInfo> linkedEntities;
	
	private AbstractCrudTest currentTest;
	
	private int random;	

	private Map<String, Map<String, Map<String, Object>>> allEntities;

	public EntityHelper(AbstractCrudTest test) {
		super();		
		this.currentTest = test;
		this.linkedEntities = new ArrayList<>();
		this.random = 0;
		this.allEntities = new HashMap<>();		
	}

	public String createSampleEntity(String url, Map<String, Object> params, String entityClassName)
			throws Exception, JsonProcessingException {
		try {
			Map<String, Map<String, Object>> map = allEntities.get(url);
			if(map != null) {
				for(Entry<String, Map<String, Object>> e  : map.entrySet()) {
					Map<String, Object> json = e.getValue();
					if(json.equals(params))
						throw new AlreadyCreatedException(e.getKey());
				}
			} else {
				map = new HashMap<>();
				allEntities.put(url, map);
			}
			ResultActions resultAction = createEntity(url, params, entityClassName);
			String location = resultAction.andReturn().getResponse().getHeader("Location");
			map.put(location, params);
			logger.debug("Entity created at "+location);	
			return location;
		} catch (MockNotFoundException e) {
			logger.debug("MockNotFoundException for entity "+entityClassName);	
			return null;
		}
	}
	
	public ResultActions createEntity(String url, Map<String, Object> jsonData, String entityClassName)
			throws Exception {
		List<EntityInfo> l = currentTest.getEntityInfoList();
		for(EntityInfo i : l) {
			if(i.getEntityClass().getName().equals(entityClassName)) {
				if(!i.isIdAuto()) {
					Long nextVal = currentTest.nextVal();
					if(Long.class.getSimpleName().equals(ReflectionUtils.getIdClass(i.getEntityClass()))) {
						jsonData.put(i.getIdField(), nextVal);
					} else {
						jsonData.put(i.getIdField(), nextVal.toString());
					}
				}
				break;
			}
		}
		logger.debug("Creating entity "+entityClassName+" at url " + url + " with data {" + jsonData + "}");		
		String body = this.currentTest.getObjectMapper().writeValueAsString(jsonData);
		logger.debug("Body = {}", body);
		ResultActions resultAction;
		MockMethod mockMethod = currentTest.getMockMethod(entityClassName, HttpMethod.POST);
		if(mockMethod != null) {
			resultAction = (ResultActions) mockMethod.getMethod().invoke(mockMethod.getInstance());
		} else { 
			resultAction = currentTest.getMockMvc(entityClassName, HttpMethod.POST).perform(
				post(url(url)).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
		}
		return resultAction;
	}


	
	
	public String url(String url) {
		if (!url.startsWith("/") && !url.startsWith("http"))
			url = "/" + url;
		return url;
	}
	
	public String createSampleEntity(EntityInfo info) throws JsonProcessingException, Exception {
		return createSampleEntity(info, false);
	}
	
	public String createSampleEntity(EntityInfo info, boolean forUpdate) throws JsonProcessingException, Exception {
		Map<String, Object> params = getParamsMap(info.getEntityClass(), forUpdate);

		return createSampleEntity(info.getPluralName(), params, info.getEntityClass().getName());
	}

	public Map<String, Object> getParamsMap(Class<?> clazz) throws Exception {
		return getParamsMap(clazz, false);
	}
	
	public void deleteEntity(String location, String entityClass) throws Exception {
		logger.debug("Deleting entity " + location);
		MockMethod mockMethod = currentTest.getMockMethod(entityClass, HttpMethod.DELETE);
		if(mockMethod != null) {
			mockMethod.getMethod().invoke(mockMethod.getInstance());
		} else { 
			this.currentTest.getMockMvc(entityClass, HttpMethod.DELETE).perform(MockMvcRequestBuilders.delete(location)).andExpect(status().is2xxSuccessful());
		}
	}

	public void createLinkedEntities(Class<?> entityClass) throws Exception {
		logger.debug("Creating linked entities for " + entityClass);
		createLinkedEntities(entityClass, true);
	}

	public void createLinkedEntities(Class<?> entityClass, boolean rootClass) throws JsonProcessingException, Exception {
		createLinkedEntities(entityClass, rootClass, false);
	}
	
	public void createLinkedEntities(Class<?> entityClass, boolean rootClass, boolean forUpdate) throws JsonProcessingException, Exception {
		createLinkedEntities(entityClass, rootClass, forUpdate, new HashSet<String>());
	}
	
	public void createLinkedEntities(Class<?> entityClass, boolean rootClass, boolean forUpdate, Set<String> processedClassNames) throws JsonProcessingException, Exception {
		logger.debug("createLinkedEntities entityClass = {}, rootClass = {}", entityClass, rootClass);
		if(!processedClassNames.add(entityClass.getName()))
			return;
		List<Field> allFields = ReflectionUtils.getAllFields(entityClass);
		for (Field f : allFields) {
			ManyToOne ann = f.getAnnotation(ManyToOne.class);
			if (ann != null) {
				Class<?> target = getTargetEntity(f, ann);
				if (!target.equals(entityClass) && !alreadyLinked(target))
					createLinkedEntities(target, false, forUpdate, processedClassNames);
			} else if(f.isAnnotationPresent(OneToMany.class) && f.isAnnotationPresent(NotEmpty.class)) {
				Class<?> target = ReflectionUtils.getGenericCollectionType(f);
				logger.debug("target = {}", target);
				if (!target.equals(entityClass) && !alreadyLinked(target))
					createLinkedEntities(target, false, forUpdate, processedClassNames);
			}
		}
		if (!rootClass) {
			String location = createSampleEntity(getEntityInfo(entityClass), forUpdate);
			if(location != null) {
				linkedEntities.add(new DeleteInfo(entityClass, location));	
			}			
		}
	}
	public boolean alreadyLinked(Class<?> target) {
		for (DeleteInfo i : linkedEntities) {
			if (i.entityClass.equals(target))
				return true;
		}
		return false;
	}

	private String getLinkedEntity(Class<?> type) {
		for (DeleteInfo i : linkedEntities) {
			if (i.entityClass.equals(type))
				return i.url;
		}
		return null;
	}

	private Class<?> getTargetEntity(Field f, ManyToOne ann) {
		Class<?> target = !ann.targetEntity().equals(void.class) ? ann.targetEntity() : f.getType();
		return target;
	}

	private EntityInfo getEntityInfo(Class<?> entityClass) {
		for (EntityInfo info : this.currentTest.getEntityInfoList()) {
			if (entityClass.equals(info.getEntityClass()))
				return info;
		}
		logger.debug("No Entity found for class {}", entityClass);
		return null;
	}

	public Map<String, Object> getParamsMap(Class<?> clazz, boolean forUpdate) throws Exception {
		Map<String, Object> params = new HashMap<>();
		List<Field> allFields = ReflectionUtils.getAllFields(clazz);
		for (Field f : allFields) {
			if (isFieldExposed(f, true) && 
					(!Collection.class.isAssignableFrom(f.getType()) || (f.isAnnotationPresent(OneToMany.class) && f.isAnnotationPresent(NotEmpty.class)))  
					&& !f.isAnnotationPresent(ManyToMany.class) 
					&& !f.getType().equals(clazz)) {
				params.put(f.getName(), getFieldValue(clazz, f, forUpdate));		
			} 			 		
		}
		return params;
	}

	

	private Object getFieldValue(Class<?> clazz, Field f, boolean forUpdate) throws Exception {
		for(EntityInfo info : this.currentTest.getEntityInfoList()) {			
			if(info.getEntityClass().equals(clazz) && info.getDataSet() != null) {
				Object value = info.getValue(f.getName(), forUpdate);				
				if(!f.isAnnotationPresent(ManyToOne.class) && !f.isAnnotationPresent(OneToMany.class)) {
					return convert(value, f);					
				} else if(value != null) {							
					Object overrideValue = info.getOverrideValue(f.getName(), forUpdate);
					if(overrideValue != null)
						return convert(overrideValue, f);	
					else {
						try {													
							Class<?> fieldClass = f.getType();
							value = createSampleEntity(value, fieldClass);
							info.setOverrideValue(f.getName(), forUpdate, value);
							return value;
						} catch(AlreadyCreatedException ace) {
							String url = ace.getUrl();
							info.setOverrideValue(f.getName(), forUpdate, url);							
							return url;
						} catch (Exception e) {
							logger.error("Not a valid JSON...", e);
							return getDefaultManyToOne(f, forUpdate, info);
						}						
					}
				}
				else {
					return getDefaultManyToOne(f, forUpdate, info);
				}
			}
		}
		Calendar cal = Calendar.getInstance();
		FieldInfo fi = new FieldInfo();
		Object value = initFieldInfo(clazz, f, cal, fi, forUpdate);
		value = verifiyValue(cal, fi, value, f);

		return value;

	}


	@SuppressWarnings("unchecked")
	private Object convert(Object value, Field f) {
		if(f.getType().isEnum()) {
			String[] names = ReflectionUtils.getNames((Class<? extends Enum<?>>)f.getType());
			if(value != null) {
				for(String s : names) {
					if(value.equals(s))
						return s;				
				}	
			}
			return names[0];
		}
		if(value == null)
			return null;
		Class<?> type = f.getType();
		if(ReflectionUtils.isBoolean(type))
			return ReflectionUtils.isBoolean(value.getClass()) ? value : Boolean.parseBoolean(value.toString());
		if(ReflectionUtils.isNumber(type)) {
			if(ReflectionUtils.isNumber(value.getClass()))
				return value;
			if(Long.class.equals(type) || Integer.class.equals(type))
				return Integer.parseInt(value.toString());
			else
				return Double.parseDouble(value.toString());
		}
		return value;
	}		


	@SuppressWarnings("unchecked")
	public String createSampleEntity(Object value, Class<?> clazz) throws JsonProcessingException, Exception {
		EntityInfo entityInfo = getEntityInfo(clazz);
		createLinkedEntities(entityInfo.getEntityClass(), true);
		Map<String, Object> map = getParamsMap(entityInfo.getEntityClass());
		Map<String, Object> valMap;
		if(value instanceof Map) 
			valMap = (Map<String, Object>) value;
		else
			valMap = this.currentTest.getObjectMapper().readValue(value.toString(), new TypeReference<HashMap<String, Object>>() {});
		if(valMap != null) {
			for(String k : map.keySet()) {
				if(!valMap.containsKey(k))
					valMap.put(k, map.get(k));
			}
		}
		String location = createSampleEntity(entityInfo.getPluralName(), valMap, entityInfo.getEntityClass().getName());
		if(location != null) {
			linkedEntities.add(new DeleteInfo(clazz, location));	
		}		
		return location;
	}


	private String getDefaultManyToOne(Field f, boolean forUpdate, EntityInfo info) {
		if (alreadyLinked(f.getType())) {
			return createLinkedEntity(forUpdate, info, f.getType());
		}
		return null;
	}


	public static ObjectMapper newObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, false);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
		return mapper;
	}

	private Object verifiyValue(Calendar cal, FieldInfo fi, Object value, Field f) throws Exception {
		String strValue = value.toString();
		if(fi.unique) {	
			if(Number.class.isAssignableFrom(f.getType()))
				value = Integer.parseInt(strValue) + (++random);
			else		
				value = strValue + (++random);
		}
		if(fi.decMin != null && fi.decMin > Double.valueOf(strValue))
			value = Double.toString(fi.decMin);
		else if (fi.decMax != null && fi.decMax > Double.valueOf(strValue))
			value = Double.toString(fi.decMax);
		else if (fi.future != null) {
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
			value = printDate(cal, f);
		} else if (fi.past != null) {
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) - 1);
			value = printDate(cal, f);
		} else if (fi.intMax != null)
			value = DatatypeConverter.printLong(fi.intMax);
		else if (fi.intMin != null)
			value = DatatypeConverter.printLong(fi.intMin);
		else if (fi.isNull != null && fi.isNull)
			value = "";
		else if (fi.pattern != null) {
			if (EXAMPLE_MAIL.equals(fi.pattern))
				value = EXAMPLE_MAIL;
			else // Didn't find any efficient library to generate sample String
				value = "";
		}
		if (fi.sizeMin != null) {
			while (strValue.length() < fi.sizeMin) {
				value = strValue + "1";
			}
		}
		if (fi.sizeMax != null) {
			while (strValue.length() > fi.sizeMax) {
				value = strValue.substring(0, strValue.length() - 1);
			}
		}
		
		return value;
	}

	@SuppressWarnings("unchecked")
	private Object initFieldInfo(Class<?> clazz, Field f, Calendar cal, FieldInfo fi, boolean forUpdate) throws Exception {
		Class<?> type = f.getType();
		Annotation[] annotations = f.getAnnotations();
		Object value = null;
		boolean nullValue = false;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(AssertTrue.class))
				value = Boolean.TRUE;
			else if (a.annotationType().equals(AssertFalse.class))
				value = Boolean.FALSE;
			else if (a.annotationType().equals(DecimalMax.class))
				fi.decMax = Double.parseDouble(((DecimalMax) a).value());
			else if (a.annotationType().equals(DecimalMin.class))
				fi.decMin = Double.parseDouble(((DecimalMin) a).value());
			else if (a.annotationType().equals(Future.class))
				fi.future = true;
			else if (a.annotationType().equals(Past.class))
				fi.past = true;
			else if (a.annotationType().equals(Min.class))
				fi.intMin = ((Min) a).value();
			else if (a.annotationType().equals(Max.class))
				fi.intMax = ((Max) a).value();
			else if (a.annotationType().equals(Null.class))
				fi.isNull = true;
			else if (a.annotationType().equals(NotNull.class))
				fi.notNull = true;
			else if (a.annotationType().equals(Pattern.class))
				fi.pattern = ((Pattern) a).regexp();
			else if (a.annotationType().equals(Email.class))
				fi.pattern = EXAMPLE_MAIL;
			else if (a.annotationType().equals(Size.class)) {
				fi.sizeMin = ((Size) a).min();
				fi.sizeMax = ((Size) a).max();
			} else if (a.annotationType().equals(Digits.class)) {
				Digits d = (Digits) a;
				fi.sizeMax = d.integer() + (d.fraction() != 0 ? d.fraction() + 1 : 0);
			} else if (a.annotationType().equals(Column.class)) {
				Column col = (Column) a;
				if (col.length() != 255) {
					fi.sizeMax = col.length();
				}
				if(col.unique())
					fi.unique = true;
			}			
			boolean remoteResourceField = RestResourceUtils.isRemoteResourceField(f);
			if (a.annotationType().equals(ManyToOne.class)) {
				if (alreadyLinked(type)) {
					value = createLinkedEntity(forUpdate, getEntityInfo(clazz), type);
				}
				else {
					value = "";
					nullValue = true;
				}
			} else if(a.annotationType().equals(OneToMany.class)) {
				if(List.class.isAssignableFrom(f.getType()))
					value = new ArrayList<Map<String, Object>>();
				else if(Set.class.isAssignableFrom(f.getType()))
					value = new HashSet<Map<String, Object>>();
				Collection<Map<String, Object>> val = (Collection<Map<String, Object>>)value;
				String collType = ReflectionUtils.getGenericCollectionType(f).getName();
				for(DeleteInfo di : linkedEntities) {
					if(di.entityClass.getName().equals(collType)) {
						String remoteObject = currentTest.getMockMvc(collType, HttpMethod.GET).perform(get(url(di.url)).contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
						val.add(currentTest.getObjectMapper().readValue(remoteObject, new TypeReference<HashMap<String, Object>>() {}));
					}
				}		
			} else if(remoteResourceField) {
				for(Field ff : ReflectionUtils.getAllFields(clazz)) {
					RestResourceMapper ann = ff.getAnnotation(RestResourceMapper.class);
					if(ann != null) {
						if(ann.resolveToProperty().equals(f.getName())) {
							if(Collection.class.isAssignableFrom(ff.getType())) {
								return new RestRemoteResource[0];									
							} else {
								return new RestRemoteResource();
							}					
						}
					}
				}
			}
		}
		if (value == null && !nullValue) {
			value = verifyNullValue(f, cal, forUpdate, type);
		}
		return value;
	}


	private Object verifyNullValue(Field f, Calendar cal, boolean forUpdate, Class<?> type) {
		Object value;
		if(ReflectionUtils.isBoolean(f.getType()))
			value = forUpdate ? false : true;
		else if(f.getType().isEnum()) {
			Object[] enumConstants = f.getType().getEnumConstants();
			value = forUpdate && enumConstants.length > 1? enumConstants[1] : enumConstants[0];
		}
		else {
			String tstStr = forUpdate ? "Test2" : "Test";
			if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
				if(f.isAnnotationPresent(Digits.class))
					value = 0;
				else
					value = forUpdate ? 7 : 9;				
			}			
			else if(f.isAnnotationPresent(Digits.class))
				value = "0";
			else if (LocalDate.class.isAssignableFrom(type))
				value = LocalDate.now().toString();
			else if (Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type))
				value = printDate(cal, f);
			else
				value = tstStr;
		}
		return value;
	}


	private String printDate(Calendar cal, Field f) {
		JsonFormat format = f.getAnnotation(JsonFormat.class);
		if(format != null && format.pattern() != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format.pattern());
			return sdf.format(cal.getTime());
		}
		return DatatypeConverter.printDate(cal);
	}


	private String createLinkedEntity(boolean forUpdate, EntityInfo info, Class<?> fieldType) {
		String value = getLinkedEntity(fieldType);
		if(forUpdate) {
			if(info.hasOnlyManyToOne()) {
				try {
					EntityInfo entityInfo = getEntityInfo(fieldType);
					value = createSampleEntity(entityInfo, true);
					if(value != null) {
						linkedEntities.add(new DeleteInfo(info.getEntityClass(), value));	
					}					
				} catch (Exception e) {
					logger.error("Exception while creating linked Entity (for Update));", e);
				}
			}
		}
		return value;
	}



	public boolean isFieldExposed(Field f, boolean includeManyToOne) {
		return !f.isAnnotationPresent(GeneratedValue.class) 
				&& !f.isAnnotationPresent(Version.class) && !f.isAnnotationPresent(JsonIgnore.class)
				&& (includeManyToOne || !f.isAnnotationPresent(ManyToOne.class));
	}
	
	public void deleteLinkedEntities(String location, String entityClass) throws Exception {
		deleteEntity(location, entityClass);
		clearLinkedEntities();
	}


	public void clearLinkedEntities() throws Exception {
		Collections.reverse(linkedEntities);
		for (DeleteInfo di : linkedEntities) {
			deleteEntity(di.url, di.entityClass.getName());
		}
		linkedEntities.clear();
	}

	public void reset() {
		for(EntityInfo info : this.currentTest.getEntityInfoList()) {
			info.reset();
		}
		random = 0;
		allEntities.clear();
	}


	public List<DeleteInfo> getLinkedEntities() {
		return linkedEntities;
	}


	public Map<String, Map<String, Map<String, Object>>> getAllEntities() {
		return allEntities;
	}
	
	

}
