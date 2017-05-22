package com.octo.tools.crud.util;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.DatatypeConverter;

import org.hibernate.validator.constraints.Email;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.tools.crud.utils.ReflectionUtils;

public class EntityHelper {

	private static final Logger logger = LoggerFactory.getLogger(EntityHelper.class);
	
	private static final String EXAMPLE_MAIL = "example@gmail.com";
	
	private MockMvc mockMvc;
	
	private ObjectMapper objectMapper;
	
	private List<DeleteInfo> linkedEntities;
	
	private List<EntityInfo> entityInfoList;
	
	private int random;
	
	public EntityHelper(MockMvc mockMvc, ObjectMapper objectMapper, List<EntityInfo> entityInfoList) {
		super();
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
		this.linkedEntities = new ArrayList<>();
		this.entityInfoList = entityInfoList;
		this.random = 0;
	}

	private String createSampleEntity(String url, Map<String, String> params)
			throws Exception, JsonProcessingException {
		ResultActions resultAction = createEntity(url, params);
		return resultAction.andReturn().getResponse().getHeader("Location");
	}
	
	public ResultActions createEntity(String url, Map<String, String> jsonData)
			throws Exception, JsonProcessingException {
		logger.debug("Creating entity at url " + url + " with data {" + jsonData + "}");
		ResultActions resultAction = this.mockMvc.perform(
				post(url(url)).contentType(MediaTypes.HAL_JSON).content(this.objectMapper.writeValueAsString(jsonData)))
				.andExpect(status().isCreated());
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
		Map<String, String> params = getParamsMap(info.getEntityClass(), forUpdate);

		return createSampleEntity(info.getPluralName(), params);
	}

	public Map<String, String> getParamsMap(Class clazz) {
		return getParamsMap(clazz, false);
	}
	
	private void deleteEntity(String location) throws Exception {
		logger.debug("Deleting entity " + location);
		this.mockMvc.perform(MockMvcRequestBuilders.delete(location)).andExpect(status().is2xxSuccessful());
	}

	public void createLinkedEntities(Class entityClass) throws Exception {
		logger.debug("Creating linked entities for " + entityClass);
		createLinkedEntities(entityClass, true);
	}

	private void createLinkedEntities(Class entityClass, boolean rootClass) throws JsonProcessingException, Exception {
		List<Field> allFields = ReflectionUtils.getAllFields(entityClass);
		for (Field f : allFields) {
			ManyToOne ann = f.getAnnotation(ManyToOne.class);
			if (ann != null) {
				Class target = getTargetEntity(f, ann);
				if (!target.equals(entityClass) && !alreadyLinked(target))
					createLinkedEntities(target, false);
			}
		}
		if (!rootClass)
			linkedEntities.add(new DeleteInfo(entityClass, createSampleEntity(getEntityInfo(entityClass), false)));
	}

	private boolean alreadyLinked(Class target) {
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

	private Class getTargetEntity(Field f, ManyToOne ann) {
		Class target = !ann.targetEntity().equals(void.class) ? ann.targetEntity() : f.getType();
		return target;
	}

	private EntityInfo getEntityInfo(Class entityClass) {
		for (EntityInfo info : entityInfoList) {
			if (entityClass.equals(info.getEntityClass()))
				return info;
		}
		return null;
	}

	public Map<String, String> getParamsMap(Class clazz, boolean forUpdate) {
		Map<String, String> params = new HashMap<>();
		List<Field> allFields = ReflectionUtils.getAllFields(clazz);
		for (Field f : allFields) {
			if (isFieldExposed(f) && !f.isAnnotationPresent(OneToMany.class)  
					&& !f.isAnnotationPresent(ManyToMany.class))
				params.put(f.getName(), getFieldValue(clazz, f, forUpdate));
		}
		return params;
	}

	

	private String getFieldValue(Class clazz, Field f, boolean forUpdate) {
		Calendar cal = Calendar.getInstance();
		FieldInfo fi = new FieldInfo();
		String value = initFieldInfo(f, cal, fi, forUpdate);
		value = verifiyValue(cal, fi, value);

		return value;

	}

	private String verifiyValue(Calendar cal, FieldInfo fi, String value) {
		if(fi.unique) {			
			value = ++random + value;
		}
		if(fi.decMin != null && fi.decMin > Double.valueOf(value))
			value = Double.toString(fi.decMin);
		else if (fi.decMax != null && fi.decMax > Double.valueOf(value))
			value = Double.toString(fi.decMax);
		else if (fi.future != null) {
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
			value = DatatypeConverter.printDate(cal);
		} else if (fi.past != null) {
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) - 1);
			value = DatatypeConverter.printDate(cal);
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
		} else {
			if (fi.sizeMin != null) {
				while (value.length() < fi.sizeMin) {
					value = value + "1";
				}
			}
			if (fi.sizeMax != null) {
				while (value.length() > fi.sizeMax) {
					value = value.substring(0, value.length() - 1);
				}
			}
		}
		return value;
	}

	private String initFieldInfo(Field f, Calendar cal, FieldInfo fi, boolean forUpdate) {
		Class<?> type = f.getType();
		Annotation[] annotations = f.getAnnotations();
		String value = null;
		boolean nullValue = false;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(AssertTrue.class))
				value = Boolean.TRUE.toString();
			else if (a.annotationType().equals(AssertFalse.class))
				value = Boolean.FALSE.toString();
			else if (a.annotationType().equals(DecimalMax.class))
				fi.decMax = Double.valueOf(((DecimalMax) a).value());
			else if (a.annotationType().equals(DecimalMin.class))
				fi.decMin = Double.valueOf(((DecimalMin) a).value());
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
			if (a.annotationType().equals(ManyToOne.class)) {
				if (alreadyLinked(type)) {
					value = getLinkedEntity(type);
					if(forUpdate) {						
						try {
							value = createSampleEntity(getEntityInfo(type), true);
							linkedEntities.add(new DeleteInfo(type, value));
						} catch (Exception e) {
							logger.error("Exception while creating linked Entity (for Update));", e);
						}
					}
					
				}
				else {
					value = "";
					nullValue = true;
				}
			}
		}
		if (value == null && !nullValue) {
			if (Boolean.class.isAssignableFrom(type) || (type.isPrimitive() && type.equals(boolean.class)))
				value = forUpdate ? "false" : "true";
			else {
				String tstStr = forUpdate ? "Test2" : "Test";
				if (String.class.isAssignableFrom(type) && !f.isAnnotationPresent(Digits.class))
					value = tstStr;
				else if(f.isAnnotationPresent(Digits.class))
					value = "0";
				else if (Number.class.isAssignableFrom(type) || type.isPrimitive())
					value = forUpdate ? "7" : "9";
				else if (Date.class.isAssignableFrom(type) || DateTime.class.isAssignableFrom(type))
					value = DatatypeConverter.printDate(cal);
				else
					value = tstStr;
			}
		}
		return value;
	}

	public boolean isFieldExposed(Field f) {
		return !f.isAnnotationPresent(Id.class) && !f.isAnnotationPresent(Transient.class)
				&& !f.isAnnotationPresent(Version.class);
	}
	
	public void deleteLinkedEntities(String location) throws Exception {
		deleteEntity(location);
		Collections.reverse(linkedEntities);
		for (DeleteInfo di : linkedEntities) {
			deleteEntity(di.url);
		}
		linkedEntities.clear();
	}
}
