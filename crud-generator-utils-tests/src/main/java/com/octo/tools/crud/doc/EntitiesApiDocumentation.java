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

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
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
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
	}
	

	@Test
	public void documentEntities() throws Exception {
		
		for (EntityInfo info : entityInfoList) {

			listExample(info);

			createExample(info);

			getExample(info);

			updateExample(info);

		}

	}

	private void updateExample(EntityInfo info) throws JsonProcessingException, Exception, NoSuchFieldException {
		entityHelper.createLinkedEntities(info.getEntityClass());
		String location = entityHelper.createSampleEntity(info);
		verifiySampleEntity(location);
		Map<String, String> paramsMap = entityHelper.getParamsMap(info.getEntityClass(), true);

		this.mockMvc
				.perform(patch(entityHelper.url(location)).contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(paramsMap)))
				.andExpect(status().isNoContent())
				.andDo(document(info.getSimpleName() + "-update-example", requestFields(
						getRequestFieldDescriptors(info.getEntityClass(), getParamsDescMap(info.getEntityClass())))));
		entityHelper.deleteLinkedEntities(location);
		entityHelper.reset();
	}

	private void getExample(EntityInfo info) throws JsonProcessingException, Exception, NoSuchFieldException {
		entityHelper.createLinkedEntities(info.getEntityClass());
		Map<String, String> paramsMap = getParamsDescMap(info.getEntityClass());
		String location = entityHelper.createSampleEntity(info);
		verifiySampleEntity(location)
				.andDo(document(info.getSimpleName() + "-get-example", links(halLinks(), getLinksForSingleItem(info)),
						responseFields(getLinkedFieldDescriptors(info.getEntityClass(), paramsMap))));
		entityHelper.deleteLinkedEntities(location);
		entityHelper.reset();
	}

	private LinkDescriptor[] getLinksForSingleItem(EntityInfo info) {

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

	private void createExample(EntityInfo info) throws Exception, JsonProcessingException, NoSuchFieldException {
		entityHelper.createLinkedEntities(info.getEntityClass());
		Map<String, String> paramsMap = entityHelper.getParamsMap(info.getEntityClass());
		ResultActions resultAction = entityHelper.createEntity(info.getPluralName(), paramsMap);
		Map<String, String> descParamsMap = getParamsDescMap(info.getEntityClass());
		resultAction.andDo(document(info.getPluralName() + "-create-example",
				requestFields(getRequestFieldDescriptors(info.getEntityClass(), descParamsMap))));
		MockHttpServletResponse response = resultAction.andReturn().getResponse();
		String location = response.getHeader("Location");
		
		entityHelper.deleteLinkedEntities(location);
		entityHelper.reset();
	}

	private FieldDescriptor[] getLinkedFieldDescriptors(Class entityClass, Map<String, String> paramsMap)
			throws NoSuchFieldException {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		for (String att : paramsMap.keySet()) {
			String desc = paramsMap.get(att);
			Field field = getField(entityClass, att);
			if(entityHelper.isFieldExposed(field)) {
				Class<?> fieldType = field.getType();
				boolean linked = ReflectionUtils.hasLinks(field);
				boolean collection = ReflectionUtils.hasCollections(field);
				boolean number = ReflectionUtils.isNumber(fieldType);
				boolean bool = ReflectionUtils.isBoolean(fieldType);
				Object jsonType = getJsonType(linked, collection, number, bool);
				String path = linked || collection ? "_embedded." + att : att;
				FieldDescriptor type = fieldWithPath(path).description(desc).type(jsonType);
				if (!isMandatory(field))
					type.optional();
				list.add(type);
			}			
		}		
		list.add(fieldWithPath("_links").description("links to other resources"));
		Collections.sort(list, (p1, p2) -> p1.getPath().compareTo(p2.getPath()));
		return list.toArray(new FieldDescriptor[0]);
	}

	public static Object getJsonType(boolean linked, boolean collection, boolean number, boolean bool) {
		Object jsonType = linked ? JsonFieldType.OBJECT
				: (collection ? JsonFieldType.ARRAY
						: (number ? JsonFieldType.NUMBER : (bool ? JsonFieldType.BOOLEAN : JsonFieldType.STRING)));
		return jsonType;
	}

	private FieldDescriptor[] getRequestFieldDescriptors(Class entityClass, Map<String, String> paramsMap)
			throws NoSuchFieldException, SecurityException {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		ConstrainedFields fields = new ConstrainedFields(entityClass);
		
		for (String att : paramsMap.keySet()) {
			String desc = paramsMap.get(att);
			Field field = getField(entityClass, att);
			if (!field.isAnnotationPresent(OneToMany.class)) {
				boolean optional = !isMandatory(field) ? true : false;
				Class<?> fieldType = field.getType();
				//For requests : all params are sent as String
				FieldDescriptor type = optional
						? fields.withPath(att).description(desc).type(JsonFieldType.STRING).optional()
						: fields.withPath(att).description(desc).type(JsonFieldType.STRING);
				list.add(type);
			}

		}
		Collections.sort(list, (p1, p2) -> p1.getPath().compareTo(p2.getPath()));
		return list.toArray(new FieldDescriptor[0]);
	}

	private Field getField(Class entityClass, String att) throws NoSuchFieldException {
		for (Field f : ReflectionUtils.getAllFields(entityClass)) {
			if (f.getName().equals(att))
				return f;
		}
		return null;

	}

	private boolean isMandatory(Field field) {
		Column col = field.getAnnotation(Column.class);
		boolean mandatory = field.isAnnotationPresent(NotNull.class);
		if (col != null)
			mandatory = !col.nullable();
		return mandatory;
	}

	private void listExample(EntityInfo info) throws JsonProcessingException, Exception {
		entityHelper.createLinkedEntities(info.getEntityClass());

		logger.debug("listExemple for " + info);
		String location = entityHelper.createSampleEntity(info);
		this.mockMvc.perform(get(entityHelper.url(info.getPluralName()))).andExpect(status().isOk())
				.andDo(document(info.getPluralName() + "-list-example", links(getLinksForList(info)),
						responseFields(getFieldsForList(info))));

		entityHelper.deleteLinkedEntities(location);
		entityHelper.reset();

	}

	private FieldDescriptor[] getFieldsForList(EntityInfo info) {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		list.add(fieldWithPath("_embedded." + info.getPluralName()).description("An array of <<resources-"
				+ info.getPluralName() + ", " + info.getSimpleName1stUpper() + " resources>>"));
		list.add(fieldWithPath("_links").description("<<resources-list-links, Links>> to other resources"));
		if (info.isPaged()) {
			list.add(fieldWithPath("page").description("Paging information (page, totalElements, totalPages, number)"));

		}
		return list.toArray(new FieldDescriptor[0]);

	}

	private LinkDescriptor[] getLinksForList(EntityInfo info) {
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

	private ResultActions verifiySampleEntity(String location) throws Exception {
		logger.debug("Getting resource at url " + location);
		return this.mockMvc.perform(get(entityHelper.url(location))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("_links.self.href", Matchers.is(location)));
	}

	public Map<String, String> getParamsDescMap(Class entityClass) {
		Map<String, String> params = new HashMap<>();
		List<Field> allFields = ReflectionUtils.getAllFields(entityClass);
		for (Field f : allFields) {
			if (entityHelper.isFieldExposed(f))
				params.put(f.getName(), MessageFormat.format(DESC_MSG, f.getName(),
						ADocEntityGenerator.getName1stLower(entityClass.getSimpleName())));
		}
		return params;
	}


	private static class ConstrainedFields {

		private final ConstraintDescriptions constraintDescriptions;

		ConstrainedFields(Class<?> input) {
			this.constraintDescriptions = new ConstraintDescriptions(input);
		}

		private FieldDescriptor withPath(String path) {
			return fieldWithPath(path).attributes(key("constraints").value(StringUtils
					.collectionToDelimitedString(this.constraintDescriptions.descriptionsForProperty(path), ". ")));
		}
	}

	
}