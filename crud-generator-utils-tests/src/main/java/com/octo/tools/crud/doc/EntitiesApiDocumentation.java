package com.octo.tools.crud.doc;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.octo.tools.common.AbstractCrudTest;
import com.octo.tools.common.MockNotFoundException;
import com.octo.tools.crud.rest.annotation.RestResourceMapper;
import com.octo.tools.crud.rest.resource.util.RestResourceUtils;
import com.octo.tools.crud.util.EntityInfo;
import com.octo.tools.crud.utils.ReflectionUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EntitiesApiDocumentation extends AbstractCrudTest {

	

	private static final Logger logger = LoggerFactory.getLogger(EntitiesApiDocumentation.class);

	public static final String REGEX_EMAIL = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
	
	private static final String DESC_MSG = "The {0} of the {1}";

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(
			ADocEntityGenerator.TARGET_GENERATED_SNIPPETS);

	protected void seUpMockMvc() {
		setDefaultMockMvc(MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build());
	}
	

	@Test
	public void documentEntities() throws Exception {
		
		for (EntityInfo info : entityInfoList) {

			try {
				listExample(info);
			} catch (MockNotFoundException e) {
				logger.debug("Disabling listExample for entity "+info.getEntityClass().getName(), e);
				entityHelper.clearLinkedEntities();
				reset();
			}

			try {
				createExample(info);
			} catch (MockNotFoundException e) {
				logger.debug("Disabling createExample for entity "+info.getEntityClass().getName(), e);
				entityHelper.clearLinkedEntities();
				reset();
			}

			try {
				getExample(info);
			} catch (MockNotFoundException e) {
				logger.debug("Disabling getExample for entity "+info.getEntityClass().getName(), e);
				entityHelper.clearLinkedEntities();
				reset();
			}

			try {
				updateExample(info);
			} catch (MockNotFoundException e) {
				logger.debug("Disabling updateExample for entity "+info.getEntityClass().getName(), e);
				entityHelper.clearLinkedEntities();
				reset();
			}

		}

	}

	protected void updateExample(EntityInfo info) throws JsonProcessingException, Exception, NoSuchFieldException {
		logger.debug("----->updateExample");
		entityHelper.createLinkedEntities(info.getEntityClass());
		String location = entityHelper.createSampleEntity(info);
		String entityClassName = info.getEntityClass().getName();
		verifiySampleEntity(location, entityClassName);
		Map<String, Object> paramsMap = entityHelper.getParamsMap(info.getEntityClass(), true);

		getMockMvc(entityClassName, HttpMethod.PATCH)
				.perform(patch(entityHelper.url(location)).contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(paramsMap)))
				.andExpect(status().isNoContent())
				.andDo(document(info.getSimpleName() + "-update-example", requestFields(
						getRequestFieldDescriptors(info.getEntityClass(), getParamsDescMap(info.getEntityClass(), true)))));
		entityHelper.deleteLinkedEntities(location, entityClassName);
		reset();
	}


	protected void getExample(EntityInfo info) throws JsonProcessingException, Exception, NoSuchFieldException {
		logger.debug("----->getExample");
		entityHelper.createLinkedEntities(info.getEntityClass());
		Map<String, String> paramsMap = getParamsDescMap(info.getEntityClass(), true);
		String location = entityHelper.createSampleEntity(info);
		String entityClassName = info.getEntityClass().getName();
		verifiySampleEntity(location, entityClassName)
				.andDo(document(info.getSimpleName() + "-get-example", 
						links(halLinks(), getLinksForSingleItem(info)),
						responseFields(getLinkedFieldDescriptors(info.getEntityClass(), paramsMap))));
		entityHelper.deleteLinkedEntities(location, entityClassName);
		reset();
	}

	protected LinkDescriptor[] getLinksForSingleItem(EntityInfo info) {

		List<LinkDescriptor> list = new ArrayList<LinkDescriptor>();
		list.add(linkWithRel("self").description(
				"Canonical link for this <<resources-" + info.getSimpleName() + "," + info.getSimpleName() + ">>"));
		list.add(linkWithRel(info.getSimpleName())
				.description("This <<resources-" + info.getSimpleName() + "," + info.getSimpleName() + ">>"));
		List<Field> allFields = ReflectionUtils.getAllFields(info.getEntityClass());
		for (Field f : allFields) {
			if (f.isAnnotationPresent(ManyToOne.class) || f.isAnnotationPresent(OneToOne.class)) {
				String name = f.getName();
				list.add(linkWithRel(name).description("The linked <<resources-" + name + "," + name + ">>"));
			} else if (f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToMany.class)) {
				String name = f.getName();
				list.add(linkWithRel(name).description("The linked list of <<resources-" + name + "," + name + ">>"));
			}
		}
		Collections.sort(list, (p1, p2) -> p1.getRel().compareTo(p2.getRel()));
		return list.toArray(new LinkDescriptor[0]);

	}

	protected void createExample(EntityInfo info) throws Exception, JsonProcessingException, NoSuchFieldException {
		logger.debug("----->createExample");
		entityHelper.createLinkedEntities(info.getEntityClass());
		Map<String, Object> paramsMap = entityHelper.getParamsMap(info.getEntityClass());		
		ResultActions resultAction = entityHelper.createEntity(info.getPluralName(), paramsMap, info.getEntityClass().getName());
		Map<String, String> descParamsMap = getParamsDescMap(info.getEntityClass(), true);
		resultAction.andDo(document(info.getPluralName() + "-create-example",
				requestFields(getRequestFieldDescriptors(info.getEntityClass(), descParamsMap))));
		MockHttpServletResponse response = resultAction.andReturn().getResponse();
		String location = response.getHeader("Location");
		
		reset(info, location);		
	}


	public void reset(EntityInfo info, String location) throws Exception {
		entityHelper.deleteLinkedEntities(location, info.getEntityClass().getName());
		reset();
	}


	public void reset() throws IOException {
		entityHelper.reset();
		initDataSets();
	}

	protected FieldDescriptor[] getLinkedFieldDescriptors(Class entityClass, Map<String, String> paramsMap)
			throws NoSuchFieldException {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		for (String att : paramsMap.keySet()) {
			String desc = paramsMap.get(att);
			Field field = getField(entityClass, att);
			boolean isManyToOne = field.isAnnotationPresent(ManyToOne.class);
			FieldDescriptor type;
			if(entityHelper.isFieldExposed(field, true)) {
				Class<?> fieldType = field.getType();
				boolean remoteResourceField = RestResourceUtils.isRemoteResourceField(field);
				if(remoteResourceField) {
					type = fieldWithPath(att).description(desc);
					FieldDescriptor transientObjectFieldDescriptor = getTransientObjectFieldDescriptor(entityClass, field, type);
					if(transientObjectFieldDescriptor != null)
						type = transientObjectFieldDescriptor;
					else
						type = type.type(JsonFieldType.STRING);
				}
				else {
					boolean collection = ReflectionUtils.hasCollections(field);
					boolean number = ReflectionUtils.isNumber(fieldType);
					boolean bool = ReflectionUtils.isBoolean(fieldType);
					Object jsonType = getJsonType(isManyToOne, collection, number, bool);					
					String path = isManyToOne || (collection && !field.isAnnotationPresent(RestResourceMapper.class)) ? "_embedded." + att : att;
					type = fieldWithPath(path).description(desc).type(jsonType);
				}
				if (isManyToOne || !isMandatory(field))
					type.optional();
				list.add(type);
			}			
		}		
		list.add(fieldWithPath("_links").description("links to other resources"));		
		Collections.sort(list, (p1, p2) -> p1.getPath().compareTo(p2.getPath()));
		return list.toArray(new FieldDescriptor[0]);
	}

	public static Object getJsonType(boolean isManyToOne, boolean collection, boolean number, boolean bool) {
		Object jsonType = isManyToOne ? JsonFieldType.OBJECT
				: (collection ? JsonFieldType.ARRAY
						: (number ? JsonFieldType.NUMBER : 
							(bool ? JsonFieldType.BOOLEAN : JsonFieldType.STRING)));
		return jsonType;
	}

	protected FieldDescriptor[] getRequestFieldDescriptors(Class entityClass, Map<String, String> paramsMap)
			throws NoSuchFieldException, SecurityException {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		ConstrainedFields fields = new ConstrainedFields(entityClass);
		
		for (String att : paramsMap.keySet()) {
			String desc = paramsMap.get(att);
			Field field = getField(entityClass, att);			
			if (!field.isAnnotationPresent(OneToMany.class)) {
				boolean optional = !isMandatory(field) ? true : false;
				Class<?> fieldType = field.getType();
				boolean remoteResourceField = RestResourceUtils.isRemoteResourceField(field);
				FieldDescriptor type = fields.withPath(att).description(desc);
				//For requests : all params are sent as String
				if(Boolean.class.equals(fieldType) || boolean.class.equals(fieldType))
					type = type.type(JsonFieldType.BOOLEAN);
				else if(Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive())
					type = type.type(JsonFieldType.NUMBER);
				else if(Collection.class.isAssignableFrom(fieldType))
					type = type.type(JsonFieldType.ARRAY);
				else if(remoteResourceField) {
					FieldDescriptor transientObjectFieldDescriptor = getTransientObjectFieldDescriptor(entityClass, field, type);
					if(transientObjectFieldDescriptor != null)
						type = transientObjectFieldDescriptor;
					else
						type = type.type(JsonFieldType.STRING);
				}
				else
					type = type.type(JsonFieldType.STRING);
						
				if(optional)
					type = type.optional();
				list.add(type);
			}

		}
		Collections.sort(list, (p1, p2) -> p1.getPath().compareTo(p2.getPath()));
		return list.toArray(new FieldDescriptor[0]);
	}


	protected FieldDescriptor getTransientObjectFieldDescriptor(Class entityClass, Field field, FieldDescriptor type) {
		for(Field ff : ReflectionUtils.getAllFields(entityClass)) {
			RestResourceMapper ann = ff.getAnnotation(RestResourceMapper.class);
			if(ann != null) {
				if(ann.resolveToProperty().equals(field.getName())) {
					if(Collection.class.isAssignableFrom(ff.getType())) {
						return type.type(JsonFieldType.ARRAY);									
					} else {
						return type.type(JsonFieldType.OBJECT);
					}					
				}
			}
		}
		return null;
	}

	protected Field getField(Class entityClass, String att) throws NoSuchFieldException {
		for (Field f : ReflectionUtils.getAllFields(entityClass)) {
			if (f.getName().equals(att))
				return f;
		}
		return null;

	}

	protected boolean isMandatory(Field field) {
		Column col = field.getAnnotation(Column.class);
		boolean mandatory = field.isAnnotationPresent(NotNull.class);
		if (col != null)
			mandatory = !col.nullable();
		return mandatory;
	}

	protected void listExample(EntityInfo info) throws JsonProcessingException, Exception {
		entityHelper.createLinkedEntities(info.getEntityClass());

		logger.debug("listExample for " + info);
		String location = entityHelper.createSampleEntity(info);
		getMockMvc(info.getEntityClass().getName(), HttpMethod.GET).perform(get(entityHelper.url(info.getPluralName()))).andExpect(status().isOk())
				.andDo(document(info.getPluralName() + "-list-example", links(getLinksForList(info)),
						responseFields(getFieldsForList(info))));

		reset(info, location);

	}

	protected FieldDescriptor[] getFieldsForList(EntityInfo info) {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		list.add(fieldWithPath("_embedded." + info.getPluralName()).description("An array of <<resources-"
				+ info.getPluralName() + ", " + info.getSimpleName1stUpper() + " resources>>"));
		list.add(fieldWithPath("_links").description("<<resources-list-links, Links>> to other resources"));
		if (info.isPaged()) {
			list.add(fieldWithPath("page").description("Paging information (page, totalElements, totalPages, number)"));

		}
		return list.toArray(new FieldDescriptor[0]);

	}

	protected LinkDescriptor[] getLinksForList(EntityInfo info) {
		List<LinkDescriptor> list = new ArrayList<LinkDescriptor>();
		list.add(linkWithRel("self").description("Canonical link for this resource"));
		list.add(linkWithRel("profile").description("The ALPS profile for this resource"));
		if (info.isSearch())
			list.add(linkWithRel("search").description("Search functions for this resource"));
		if (info.isPaged()) { 
			list.add(linkWithRel("first").optional().description("The first page of results")); 
			list.add(linkWithRel("last").optional().description("The last page of results")); 
			list.add(linkWithRel("next").optional().description("The next page of results")); 
			list.add(linkWithRel("prev").optional().description("The previous page of results")); 
		}
		return list.toArray(new LinkDescriptor[0]);
	}

	protected ResultActions verifiySampleEntity(String location, String entityClassName) throws Exception {
		logger.debug("Getting resource at url " + location);
		return getMockMvc(entityClassName, HttpMethod.GET).perform(get(entityHelper.url(location))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("_links.self.href", Matchers.is(location)));
	}

	public Map<String, String> getParamsDescMap(Class entityClass, boolean includeManyToOne) {
		Map<String, String> params = new HashMap<>();
		List<Field> allFields = ReflectionUtils.getAllFields(entityClass);
		for (Field f : allFields) {
			if (entityHelper.isFieldExposed(f, includeManyToOne))
				params.put(f.getName(), MessageFormat.format(DESC_MSG, f.getName(),
						getName1stLower(entityClass.getSimpleName())));
		}
		return params;
	}


	protected static class ConstrainedFields {

		private final ConstraintDescriptions constraintDescriptions;

		public ConstrainedFields(Class<?> input) {
			this.constraintDescriptions = new ConstraintDescriptions(input);
		}

		public FieldDescriptor withPath(String path) {
			return fieldWithPath(path).attributes(key("constraints").value(StringUtils
					.collectionToDelimitedString(this.constraintDescriptions.descriptionsForProperty(path), ". ")));
		}
	}

	
}