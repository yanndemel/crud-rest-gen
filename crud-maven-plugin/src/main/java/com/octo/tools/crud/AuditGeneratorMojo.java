package com.octo.tools.crud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.hibernate.envers.Audited;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import com.octo.tools.audit.AbstractDefaultAuditController;
import com.octo.tools.audit.AbstractReflectionAuditController;
import com.octo.tools.crud.utils.ReflectionUtils;
import com.octo.tools.crud.utils.StringUtils;

/**
 * Goal which generates Audit Controllers for retrieving audit information of @Audited (Hibernate Envers) JPA entities
 *
 */
@Mojo(name = "audit", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AuditGeneratorMojo extends AbstractMojo {

	
	@Parameter(defaultValue = "${project}")
    private MavenProject project;
	
	/**
     * Name of the persistent unit defined in the persistence.xml
     */
	@Parameter(property = "persistentUnitName", required = true)
	private String persistentUnitName;

	/**
	 * Location of the generated files
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/", property = "outputDir", required = false)
	private String outputDirectory;

	/**
	 * Name of the destination package of the sources
	 */
	@Parameter(property = "packageName", required = true)
	private String packageName;
	
	/**
	 * Boolean for adding generated sources to the compilation path
	 */
	@Parameter(property = "compile", defaultValue = "true", required = false)
	private boolean compile;
	
	
	/**
	 * Class name of the custom abstract audit controller to use as superclass for all audit controllers
	 */
	@Parameter(property = "auditControllerClassName", required = false)
	private String auditControllerClassName;
	

	public void execute() throws MojoExecutionException {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistentUnitName);
		EntityManager em = emf.createEntityManager();

		try {
			generateAuditControllers(em);
			if(compile)
				project.addCompileSourceRoot(outputDirectory);
		} catch (Exception e) {
			throw new MojoExecutionException("Exception during API generation", e);
		}

	}

	/*
	 * Method used to generate <Entity>AuditController java source code for all @Autited Entities
	 *  --> can be used as a unit test by adding @Test on the method : take care at commenting the @Test before committing your code 
	 *  !!! WARNING !!! Overrides existing classes in the source code 
	 *  You will need to refresh the package ${packageName}.audit after execution. 
	 *  The test is based on a template : AuditController.template
	 *   
	 * 
	 * */
	public void generateAuditControllers(EntityManager em) throws Exception {
		File dir = new File(getRootDirectoryPath() + "/audit/");
		if (!dir.exists())
			dir.mkdirs();
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		Class revisionEntityClass = getRevisionEntityClass(em);
		String auditControllerShortName = null;
		String revisionEntityParameter = "";
		String importRevisionClass = "";
		if(auditControllerClassName == null) {
			if(revisionEntityClass.equals(DefaultRevisionEntity.class)) {
				auditControllerClassName = AbstractDefaultAuditController.class.getName();
				auditControllerShortName = AbstractDefaultAuditController.class.getSimpleName();
			}						
			else {
				System.err.println("AuditGeneratorMojo : RevisionEntity in your model is not DefaultRevisionEntity. You should provide parameter auditControllerClassName in order to define "
						+ "a custom AbstractAuditController for your entities (see example in doc).\n"
						+ "The com.octo.tools.audit.AbstractReflectionAuditController<T, R> will be used (should be replaced by a custom auditController extending "
						+ "com.octo.tools.audit.AbstractAuditController<T, CustomRevisionEntity> to avoid reflection).");
				auditControllerClassName = AbstractReflectionAuditController.class.getName();
				auditControllerShortName = AbstractReflectionAuditController.class.getSimpleName();
				revisionEntityParameter = ", " + revisionEntityClass.getSimpleName();
				importRevisionClass = "import "+revisionEntityClass.getName() + ";";
			}
		}
		if(auditControllerShortName == null) {
			auditControllerShortName = Class.forName(auditControllerClassName).getSimpleName();
		}
		for (EntityType type : entityList) {
			Class javaType = type.getJavaType();
			if (ReflectionUtils.isEntityExposed(javaType) && javaType.isAnnotationPresent(Audited.class)) {
				String filename = javaType.getSimpleName() + "AuditController.java";
				Path path = Paths.get(dir.getPath(), filename);
				System.out.println("File " + path);
				BufferedWriter writer = Files.newBufferedWriter(path);
				InputStream in = getClass().getClassLoader().getResourceAsStream("AuditController.template");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				while (reader.ready()) {
					String s = reader.readLine();
							s = s.replaceAll("\\$\\$PACKAGE\\$\\$", packageName)
									.replaceAll("\\$\\$IMPORT_REVISION_CLASS\\$\\$", importRevisionClass)
									.replaceAll("\\$\\$AUDIT_CONTROLLER_CLASS\\$\\$", auditControllerClassName)
									.replaceAll("\\$\\$AUDIT_CONTROLLER\\$\\$", auditControllerShortName)
									.replaceAll("\\$\\$ENTITY\\$\\$", javaType.getSimpleName())
									.replaceAll("\\$\\$REVISION_ENTITY\\$\\$", revisionEntityParameter)
									.replaceAll("\\$\\$ENTITY_CLASS\\$\\$", javaType.getName())
									.replaceAll("\\$\\$ENTITY_UPPER_PLURAL\\$\\$",  StringUtils.plural(javaType.getSimpleName()).toUpperCase());
								writer.write(s + "\n");
				}
				reader.close();
				writer.close();
			}
		}
		
	}


	private Class getRevisionEntityClass(EntityManager em) {
		Set<EntityType<?>> entities = em.getMetamodel().getEntities();
		for(EntityType entity : entities) {
			if(entity.getJavaType() != null && entity.getJavaType().isAnnotationPresent(RevisionEntity.class)) {
				return  entity.getJavaType();
			}
		}
		return DefaultRevisionEntity.class;
	}

	private String getRootDirectoryPath() {
		return outputDirectory + packageName.replaceAll("\\.", "/");
	}


}
