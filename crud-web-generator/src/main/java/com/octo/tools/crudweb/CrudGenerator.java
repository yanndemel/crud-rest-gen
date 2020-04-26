package com.octo.tools.crudweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Persistence;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.persistence.metamodel.EntityType;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.octo.tools.crud.utils.ReflectionUtils;
import com.octo.tools.crud.utils.StringUtils;


public class CrudGenerator {
	
	private String resourcesDir = "./";
	
	private VelocityEngine ve;

	private String restUrl;

	private String logoPath;
	
	private String welcomeMsg;
	
	private String contextPath;

	private TreeMap<String, Map<String, String>> entitiesByPackage;

	public void generate(String persistenceUnitName, String destDirRelativePath, 
			String restUrl, String logoPath, String contextPath, String welcomeMsg) throws Exception {
		
		assert(persistenceUnitName != null);
		assert(destDirRelativePath != null);
		assert(restUrl != null);
		assert(logoPath != null);
		assert(contextPath != null);
		assert(welcomeMsg != null);
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);
		EntityManager em = emf.createEntityManager();
		
        getVe();
        
        this.restUrl = restUrl;
        
        this.logoPath = logoPath;
        
        this.welcomeMsg = welcomeMsg;
        
        this.contextPath = contextPath;
        
        List<Map<String, Object>> entities = initPersistenceInfo(em);
        
        File root = init(destDirRelativePath);
        
        generateAppAndSrvJS(ve, entities, root);
		
        generateControllersAndModulesJS(ve, entities, root);
        
        generateHTML(ve, entities, root);
        
        System.out.println("CRUD Web app generated in "+root.getAbsolutePath());
		
         
	}

	public File init(String destDirRelativePath) throws IOException, URISyntaxException {
		File root = new File(destDirRelativePath);        
        root.delete();
        root.mkdirs();
        
        copyResources(root);
        
        ve.init();
		return root;
	}

	public VelocityEngine getVe() {
		this.ve  = new VelocityEngine();
        return ve;
	}

	private void copyResources(File root) throws IOException, URISyntaxException {
		File srcDir = new File(resourcesDir + "static");
		if(srcDir.exists() && srcDir.isDirectory())
			FileUtils.copy(srcDir, root);
		else {
			FileUtils.copyResourcesToDirectory("static", root.getPath());
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		}
	}



	public void generateAppAndSrvJS(VelocityEngine ve, List<Map<String, Object>> entities, File root)
			throws IOException, ResourceNotFoundException, ParseErrorException, URISyntaxException {
		File js = new File(root, "js");
        if(!js.exists())
        	js.mkdir();
        
        VelocityContext context = newVelocityContext();
        context.put("entities", entities);
        context.put("restUrl", restUrl);
        
        Template appTemplate = ve.getTemplate(getResourceFile("js/app_js.vm") );
        Path path = Paths.get(js.getPath(), "app.js");
		System.out.println("File " + path);
		BufferedWriter writer = Files.newBufferedWriter(path);
		appTemplate.merge( context, writer );
		writer.close();
		
		Template srvTemplate = ve.getTemplate(getResourceFile("js/services_js.vm") );
        path = Paths.get(js.getPath(), "services.js");
		System.out.println("File " + path);
		writer = Files.newBufferedWriter(path);
		srvTemplate.merge( context, writer );
		writer.close();
	}

	private VelocityContext newVelocityContext() {
		VelocityContext context = new VelocityContext();
		context.put("adminPrefix", contextPath);
		return context;
	}

	private String getResourceFile(String relativePath) throws URISyntaxException {
		File f = new File(resourcesDir + "templates/" + relativePath);
		if(f.exists() && f.isFile())
			return f.getPath();
		else
			return "templates/" + relativePath;
	}
	
	public void generateHTML(VelocityEngine ve, List<Map<String, Object>> entities, File root)
			throws IOException, ResourceNotFoundException, ParseErrorException, URISyntaxException {
		File partials = new File(root, "partials");
		if(!partials.exists())
        	partials.mkdir();
              
        Template formTemplate = ve.getTemplate(getResourceFile("page/ENTITY_form_html.vm"));
        Template listTemplate = ve.getTemplate(getResourceFile("page/ENTITY_list_html.vm"));
        VelocityContext context;
		generateHTML(entities, partials, formTemplate, listTemplate);
        
        Template indexTemplate = ve.getTemplate(getResourceFile("page/index_html.vm"));
        context = newVelocityContext();
        context.put("entities", entities);   
        context.put("entitiesByPackage", entitiesByPackage);   
        context.put("logoPath", logoPath);
        Path path = Paths.get(root.getPath(), "index.html");
		System.out.println("File " + path);
		BufferedWriter writer = Files.newBufferedWriter(path);
		indexTemplate.merge( context, writer );
		writer.close();
		
		Template welcomeTemplate = ve.getTemplate(getResourceFile("page/welcome_html.vm"));
        context = newVelocityContext();
        context.put("logoPath", logoPath);
        context.put("welcomeMsg", welcomeMsg);
        path = Paths.get(root.getPath(), "partials", "welcome.html");
		System.out.println("File " + path);
		writer = Files.newBufferedWriter(path);
		welcomeTemplate.merge( context, writer );
		writer.close();
        
	}

	public void generateHTML(List<Map<String, Object>> entities, File partials, Template formTemplate,
			Template listTemplate) throws IOException {
		VelocityContext context = newVelocityContext();
        for(Map<String, Object> map : entities) {
        	String name = (String) map.get("name");
        	File dir = new File(partials, name.toLowerCase());
        	if(!dir.exists())
        		dir.mkdir();
        	context.put("entity", map);
            Path path = Paths.get(dir.getPath(), name.toLowerCase() + "_form.html");
    		System.out.println("File " + path);
    		BufferedWriter writer = Files.newBufferedWriter(path);
    		formTemplate.merge( context, writer );
    		writer.close();
    		path = Paths.get(dir.getPath(), name.toLowerCase() + "_list.html");
    		System.out.println("File " + path);
    		writer = Files.newBufferedWriter(path);
    		listTemplate.merge( context, writer );
    		writer.close();
        }
	}
	
	public void generateControllersAndModulesJS(VelocityEngine ve, List<Map<String, Object>> entities, File root)
			throws IOException, ResourceNotFoundException, ParseErrorException, URISyntaxException {
		File js = new File(root, "js");
		if(!js.exists())
        	js.mkdir();
              
        Template ctlrTemplate = ve.getTemplate(getResourceFile("js/ENTITY_controller_js.vm"));
        Template moduleTemplate = ve.getTemplate(getResourceFile("js/ENTITY_module_js.vm"));
        VelocityContext context = newVelocityContext();
        for(Map<String, Object> map : entities) {
        	String name = (String) map.get("name");
        	File dir = new File(js, name.toLowerCase());
        	if(!dir.exists())
        		dir.mkdir();
        	context.put("entity", map);
            Path path = Paths.get(dir.getPath(), name.toLowerCase() + "_controller.js");
    		System.out.println("File " + path);
    		BufferedWriter writer = Files.newBufferedWriter(path);
    		ctlrTemplate.merge( context, writer );
    		writer.close();
    		path = Paths.get(dir.getPath(), name.toLowerCase() + "_module.js");
    		System.out.println("File " + path);
    		writer = Files.newBufferedWriter(path);
    		moduleTemplate.merge( context, writer );
    		writer.close();
        }
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> initPersistenceInfo(EntityManager em) throws ClassNotFoundException {
		Set<EntityType<?>> allEntities = em.getMetamodel().getEntities();
		entitiesByPackage = new TreeMap<>();
        Map<String, Object> entityInfo;        
        List<Map<String, Object>> entities = new ArrayList<Map<String,Object>>();
        for(EntityType<?> et : allEntities) {
        	Class<?> clazz = et.getJavaType();
        	if(ReflectionUtils.isEntityExposed(clazz)) {
        		entityInfo = new HashMap<String, Object>();
            	entities.add(entityInfo);            	
            	String packageName = clazz.getPackage().getName();
        		String name = clazz.getSimpleName();
        		entityInfo.put("entityClass", clazz);
				entityInfo.put("name", name);
            	entityInfo.put("uncapitName", uncapitalize(name));
            	entityInfo.put("pluralName", uncapitalize(StringUtils.plural(name)));
            	List<Field> fields = ReflectionUtils.getAllFields(clazz);
            	List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
            	AtomicBoolean hasLink = new AtomicBoolean(false);
            	AtomicBoolean hasColl = new AtomicBoolean(false);
            	fields.stream().filter(f -> !f.isAnnotationPresent(JsonIgnore.class))
				.forEach(f -> {
					
            		FieldInfo fi = null;
            		if(f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToMany.class)) {
            			try {
							fi = new FieldInfo(f.getName(), ReflectionUtils.getGenericCollectionTypeName(f), true, false);
						} catch (ClassNotFoundException e) {
							System.err.println(e.getMessage());
							return;
						}
            			hasColl.set(true);
            		}
            		else if(f.isAnnotationPresent(ManyToOne.class) || f.isAnnotationPresent(OneToOne.class)) {
            			fi = new FieldInfo(f.getName(), f.getType().getSimpleName(), false, true, f.getType());
            			hasLink.set(true);
            		}
            		else if(!f.isAnnotationPresent(Id.class) && !f.isAnnotationPresent(Version.class)
            				&& !f.isAnnotationPresent(Transient.class)) {
            			fi = new FieldInfo(f.getName(), f.getType().getSimpleName(), false, false);
            		}
            		if(fi != null) {
            			fieldInfoList.add(fi);
            			if(f.isAnnotationPresent(NotNull.class))
            				fi.setNotNull(true);
            			if(f.isAnnotationPresent(Size.class)) {
            				Size size = f.getAnnotation(Size.class);
            				fi.setSizeMax(size.max());
            				fi.setSizeMin(size.min());            				
            			} else if(f.isAnnotationPresent(Digits.class)) {
            				Digits d = f.getAnnotation(Digits.class);
            				fi.setSizeMax(d.integer() + (d.fraction() > 0 ? 1 + d.fraction() : 0));
            				double max = (Math.pow(10, d.integer())) - (d.fraction() == 0 ? 1 : Math.pow(10, -1 * d.fraction()));
            				fi.setStep((d.fraction() == 0 ? 1 : Math.pow(10, -1 * d.fraction())));
							fi.setMax(max);
            			}
            			if(f.isAnnotationPresent(Column.class)) {
            				Column col = f.getAnnotation(Column.class);
            				if(col.unique())
            					fi.setUnique(true);
            				if(!col.nullable())
            					fi.setNotNull(true);
            				if(fi.getSizeMax() == 255)
            					fi.setSizeMax(col.length());      
            				if("text".equals(col.columnDefinition())) {
            					fi.setSizeMax(4000);
            				}
            			}
            			if(f.isAnnotationPresent(Lob.class)) {
            				fi.setSizeMax(4000);
            			}
            			if(Number.class.isAssignableFrom(f.getType()) || 
            					(f.getType().isPrimitive() && (f.getType().equals(int.class) || f.getType().equals(double.class) || f.getType().equals(long.class) || f.getType().equals(short.class)))) {
            				fi.setNumber(true);
            				if(f.isAnnotationPresent(Max.class))
            					fi.setMax(Double.valueOf((f.getAnnotation(Max.class)).value()));
            				if(f.isAnnotationPresent(Min.class))
            					fi.setMin(Double.valueOf((f.getAnnotation(Min.class)).value()));
            				if(f.isAnnotationPresent(DecimalMax.class))
            					fi.setMax(Double.valueOf((f.getAnnotation(DecimalMax.class)).value()));
            				if(f.isAnnotationPresent(DecimalMin.class))
            					fi.setMin(Double.valueOf((f.getAnnotation(DecimalMin.class)).value()));
            				
            			}
            			if(!fi.isCollection() && Collection.class.isAssignableFrom(f.getType())) {            		
            				fi.setList(true);
            			}
            			if(f.getType().isEnum()) {
            				fi.setEnumField(true);
            				fi.setEnumValues(Arrays.asList(ReflectionUtils.getNames((Class<? extends Enum<?>>) f.getType())));
            			}
            		}
            	});
            	Collections.sort(fieldInfoList, (p1, p2) -> p1.getName().compareTo(p2.getName()));
            	entityInfo.put("info", fieldInfoList);
            	entityInfo.put("hasLinks", hasLink.get());
            	entityInfo.put("hasCollections", hasColl.get());
            	entityInfo.put("singleTableInheritance", ReflectionUtils.isSingleTableInheritance(clazz));
            	String parentPackage = packageName.substring(packageName.lastIndexOf(".")+1);
            	
            	StringBuilder sb = new StringBuilder(parentPackage.substring(0, 1).toUpperCase()).append(parentPackage.substring(1));
				addToEntitiesByPackage(sb.toString(), entityInfo);
        	}
        	
        	
        }       
        entities.forEach( map -> {
        	if(true == (boolean)map.get("hasLinks")) {
        		List<FieldInfo> fieldInfoList = (List<FieldInfo>) map.get("info");
        		fieldInfoList.forEach(f -> {
        			if(f.isLink() && ReflectionUtils.isSingleTableInheritance(f.getFieldType())) {
        				f.setChildEntities(getChildEntities(f.getFieldType(), entities));
        			}
        		});
        	}
        });
        Collections.sort(entities, (p1, p2) -> ((String)p1.get("name")).compareTo((String)p2.get("name")));               
		return entities;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<String> getChildEntities(Class entityClass, List<Map<String, Object>> list) {
		return list.stream().filter((map)->{
			Class subClass = (Class)map.get("entityClass");
			return entityClass.isAssignableFrom(subClass) 
						&& subClass.isAnnotationPresent(DiscriminatorValue.class);
					}).map(entity -> (String)entity.get("uncapitName")).collect(Collectors.toList());
	}

	private void addToEntitiesByPackage(String name, Map<String, Object> entityInfo) {
		if(ReflectionUtils.isSingleTableInheritance((Class<?>) entityInfo.get("entityClass")))
				return;
		Map<String, String> map = entitiesByPackage.get(name);
		if(map == null) {
			map = new TreeMap<>();
			entitiesByPackage.put(name, map);
		}
		map.put((String)entityInfo.get("uncapitName"), (String)entityInfo.get("name"));
		
	}

	private String uncapitalize(String name) {
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	public void setResourcesDir(String resourcesDir) {
		this.resourcesDir = resourcesDir;
	}

	

}
