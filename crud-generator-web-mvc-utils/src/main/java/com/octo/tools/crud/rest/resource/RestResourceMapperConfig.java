package com.octo.tools.crud.rest.resource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.octo.tools.crud.rest.annotation.RestResourceMapper;
import com.octo.tools.crud.rest.resource.util.FieldInfo;
import com.octo.tools.crud.rest.resource.util.RestResourceUtils;
import com.octo.tools.crud.utils.ReflectionUtils;

@Configuration
public class RestResourceMapperConfig {

	private static final Logger logger = LoggerFactory.getLogger(RestResourceMapperConfig.class);
	
	@Autowired
	private EntityManagerFactory emf;
	
	@Autowired
	private RestResourceMapperService restResourceMapperService;

	private Map<String, List<FieldInfo>> fieldGetterSetterByClassName;

	@PostConstruct
	public void postConstruct() throws IntrospectionException, ConfigurationException {
		fieldGetterSetterByClassName = new HashMap<>();
		initAnnotatedRestResources();
	}

	private void initAnnotatedRestResources() throws IntrospectionException, ConfigurationException {
		EntityManager em = emf.createEntityManager();
		try {
			Set<EntityType<?>> entities = em.getMetamodel().getEntities();
			for (EntityType<?> type : entities) {
				Class<?> javaType = type.getJavaType();
				if (ReflectionUtils.isEntityExposed(javaType)) {
					List<FieldInfo> l = new ArrayList<>();
					BeanInfo beanInfo = Introspector.getBeanInfo(javaType);
					List<Field> fields = ReflectionUtils.getAllFields(javaType);
					PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
					for (Field f : fields) {
						RestResourceMapper a = f.getAnnotation(RestResourceMapper.class);
						if (a != null) {
							PropertyDescriptor pd = ReflectionUtils.getPropertyDescriptor(pds, f);
							if (pd != null && pd.getReadMethod() != null
									&& Modifier.isPublic(pd.getReadMethod().getModifiers())) {
								l.add(new FieldInfo(f.getName(), a, pd.getReadMethod(),
										RestResourceUtils.getWriteMethod(fields, pds, a.resolveToProperty())));
							}
						}
					}
					if (!l.isEmpty())
						fieldGetterSetterByClassName.put(javaType.getName(), l);
				}
			}
		} finally {
			em.close();
		}
	}

	@Bean
	public RepresentationModelProcessor<EntityModel<?>> resourceProcessor() {

		return new RepresentationModelProcessor<EntityModel<?>>() {
			@Override
			public EntityModel<?> process(EntityModel<?> resource) {

				if(resource instanceof PersistentEntityResource) {
					PersistentEntityResource ress = (PersistentEntityResource)resource;
					List<FieldInfo> list = fieldGetterSetterByClassName.get(ress.getPersistentEntity().getName());
					if (list != null) {
						for (FieldInfo info : list) {
							try {
								Object content = ress.getContent();	
								Object resourceObject;
								if(AopUtils.isJdkDynamicProxy(content)) {
									try {
										resourceObject = Proxy.getInvocationHandler(content)
												.invoke(content, info.getFieldGetter(), null);
									} catch (Throwable e) {
										logger.error("Exception while un-proxing resource", e);
										return resource;
									}
								} else
									resourceObject = info.getFieldGetter().invoke(content);
								if(resourceObject != null) {
									RestResourceMapper annotation = info.getAnnotation();
									if(info.isCollection()) {
										List<RestRemoteResource> resolvedResources = null;
										if (annotation.resolveToProperty() != null)
											resolvedResources = new ArrayList<>();
										Collection<?> coll = (Collection<?>) resourceObject;
										for(Object oId : coll) {
											String resourceURL = restResourceMapperService.getResourceURL(annotation, oId);
											if (annotation.resolveToProperty() != null) {
												resolvedResources.add(restResourceMapperService.getResolvedResource(resourceURL, annotation, oId));										
											} else {
												addLink(resource, info, annotation, resourceURL);
											}
										}
										if (annotation.resolveToProperty() != null) {
											try {
												setResolvedResource(resource, info, content, resolvedResources);
											} catch (ProxyException e) {
												return resource;
											}		
										}
									} else {
										// construct a REST endpoint URL from the annotation
										// properties and resource id
										String resourceURL = restResourceMapperService.getResourceURL(annotation, resourceObject);
										if (annotation.resolveToProperty() != null) {
											// for eager fetching, fetch the resource and embed
											// its contents within the designated property
											// no links are added
											RestRemoteResource resolvedResource = restResourceMapperService.getResolvedResource(resourceURL,annotation, resourceObject);
											try {
												setResolvedResource(resource, info, content, resolvedResource);
											} catch (ProxyException e) {
												return resource;
											}
										} else {
											addLink(resource, info, annotation, resourceURL);
										}
									}	
								}							
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
									| IOException | HttpRequestException e) {
								logger.error("Exception while fetching remote resources", e);
							}
						}
					}
					Links links = resource.getLinks();
					if(links != null) {
						Optional<Link> link = resource.getLink("self");
						if(link.isPresent()) {
							Iterator<Link> it = links.iterator();
							boolean ok = true;
							while(it.hasNext() && ok) {
								Link l = it.next();
								if(l.getRel().value().equalsIgnoreCase(ress.getPersistentEntity().getType().getSimpleName())
										&& l.getHref().equals(link.get().getHref())) {
									it.remove();
									ok = false;
								}
							}
						}
					}
				}				
				return resource;
			}	
		};
	}

	/**
	 * for external links, we simply want to put the
		constructed URL into the JSON output
		for internal links, we want to ensure that the
		 URL conforms to HATEOAS for the given resource
	 * */
	private void addLink(EntityModel<?> resource, FieldInfo info, RestResourceMapper annotation, String resourceURL)
			throws MalformedURLException {
		resource.add(new Link(info.getFieldName(),
				annotation.external() ? resourceURL
						: restResourceMapperService.getHATEOASURLForResource(resourceURL,
								resource.getContent().getClass())));
	}

	private void setResolvedResource(EntityModel<?> resource, FieldInfo info, Object content, Object resolvedResource)
			throws IOException, JsonParseException, JsonMappingException, IllegalAccessException,
			InvocationTargetException, ProxyException {
		Method setter = info.getPropertySetter();
		if(AopUtils.isJdkDynamicProxy(content)) {
			try {
				Proxy.getInvocationHandler(content)
						.invoke(content, setter, new Object[] {resolvedResource});
			} catch (Throwable e) {
				logger.error("Exception while un-proxing resource", e);
				throw new ProxyException(e);
			}
		} else
			setter.invoke(resource.getContent(),
					resolvedResource);
	}

}
