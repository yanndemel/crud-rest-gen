package com.octo.tools.crud;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Version;
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

import com.octo.tools.crud.utils.ReflectionUtils;
import com.octo.tools.crud.utils.StringUtils;

/**
 * Goal which generates all RestRepositoryResources from the JPA entities of the model
 *
 */
@Mojo(name = "crudapi", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CrudApiGeneratorMojo extends AbstractMojo {

	
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
	 * Set to false if you don't want Projections (*Excerpt.java source files) to be generated
	 */
	@Parameter(property = "projections", defaultValue = "true", required = false)
	private boolean projections;
	
	/**
	 * Set to false if you don't want to add generated sources to the compilation path
	 */
	@Parameter(property = "compile", defaultValue = "true", required = false)
	private boolean compile;
	
	public void execute() throws MojoExecutionException {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistentUnitName);
		EntityManager em = emf.createEntityManager();

		try {
			generateURLsContstants(em);
			if(projections)
				generateProjectionExcepts(em);
			generateRepositories(em);
			if(compile)
				project.addCompileSourceRoot(outputDirectory);
		} catch (Exception e) {
			throw new MojoExecutionException("Exception during API generation", e);
		}

	}

	/*
	 * Method used to generate Exceprt java source code in 
	 * ${packageName}.projection 
	 * 
	 */
	private void generateProjectionExcepts(EntityManager em) throws Exception {
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		for (EntityType type : entityList) {
			Class javaType = type.getJavaType();
			if (ReflectionUtils.isEntityExposed(javaType)) {
				String filename = javaType.getSimpleName() + "Excerpt" + ".java";
				File dir = new File(getRootDirectoryPath() + "/projection");
				if (!dir.exists())
					dir.mkdirs();
				Path path = Paths.get(dir.getPath(), filename);
				System.out.println("File " + path);

				StringBuilder projection = new StringBuilder();
				BeanInfo beanInfo = Introspector.getBeanInfo(javaType);
				List<Field> fields = ReflectionUtils.getAllFields(javaType);
				PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
				String lastField = null;
				for (Field f : fields) {
					if (!f.isAnnotationPresent(Id.class)) {
						PropertyDescriptor pd = ReflectionUtils.getPropertyDescriptor(pds, f);
						if (pd != null && pd.getReadMethod() != null
								&& Modifier.isPublic(pd.getReadMethod().getModifiers())) {
							if (f.isAnnotationPresent(Version.class))
								lastField = getMethodAsString(pd, f, javaType);
							else
								projection.append(getMethodAsString(pd, f, javaType));
						}
					}
				}
				if (lastField != null)
					projection.append(lastField);
				projection.append(getComment("Short label of " + javaType.getSimpleName(),
						"the short label of " + javaType.getSimpleName()));
				projection.append("\t@Value(\"#{T(com.octo.tools.crud.utils.StringUtils).toString(target)}\")\n");
				projection.append("\tpublic String getShortLabel();\n\n");

				BufferedWriter writer = Files.newBufferedWriter(path);
				InputStream in = getClass().getClassLoader().getResourceAsStream("Excerpt.template");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				while (reader.ready()) {
					String s = reader.readLine();
					s = s.replaceAll("\\$\\$PACKAGE\\$\\$", packageName)
							.replaceAll("\\$\\$ENTITY\\$\\$", javaType.getSimpleName())
							.replaceAll("\\$\\$ENTITY_CLASS\\$\\$", javaType.getName())
							.replaceAll("\\$\\$PROJECTION\\$\\$", projection.toString());
					writer.write(s + "\n");
				}
				reader.close();
				writer.close();
			}
		}
	}

	private void generateRepositories(EntityManager em) throws MojoExecutionException, IOException {
		File dir = getRootDirectory();

		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		for (EntityType type : entityList) {
			Class javaType = type.getJavaType();
			if (ReflectionUtils.isEntityExposed(javaType)) {
				String filename = javaType.getSimpleName() + "Repository.java";
				Path path = Paths.get(dir.getPath(), filename);
				System.out.println("File " + path);
				BufferedWriter writer = Files.newBufferedWriter(path);
				InputStream in = getClass().getClassLoader().getResourceAsStream(projections ? "Repository.template" : "Repository.noProjection.template");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				while (reader.ready()) {
					String s = reader.readLine();
					s = s.replaceAll("\\$\\$PACKAGE\\$\\$", packageName)
							.replaceAll("\\$\\$ENTITY\\$\\$", javaType.getSimpleName())
							.replaceAll("\\$\\$ENTITY_CLASS\\$\\$", javaType.getName());
					writer.write(s + "\n");
				}
				reader.close();
				writer.close();
			}
		}
	}

	private File getRootDirectory() {
		File dir = new File(getRootDirectoryPath());
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	private String getRootDirectoryPath() {
		return outputDirectory + packageName.replaceAll("\\.", "/");
	}

	/*
	 * 
	 * Generate package_name.URLs source code
	 */
	private void generateURLsContstants(EntityManager em) throws Exception {
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();

		File dir = getRootDirectory();
		String filename = "URLs.java";
		Path path = Paths.get(dir.getPath(), filename);
		System.out.println("File " + path);
		try {
			BufferedWriter writer = Files.newBufferedWriter(path);
			writer.write("package "+packageName+";\n\n");
			writer.write("public interface URLs {\n\n");
			for (EntityType type : entityList) {
				Class javaType = type.getJavaType();
				if (ReflectionUtils.isEntityExposed(javaType)) {
					String plural = StringUtils.plural(javaType.getSimpleName());
					writer.write("\tpublic String " + plural.toUpperCase() + " = \"/" + plural + "\";\n");
					writer.write("\tpublic String " + plural.toUpperCase() + "_ID = \"/" + plural + "/{id}\";\n");
					writer.write(
							"\tpublic String " + plural.toUpperCase() + "_SEARCH = \"/" + plural + "/search\";\n\n");
				}
			}
			writer.write("}");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getMethodAsString(PropertyDescriptor pd, Field f, Class javaType) {
		if (pd.getReadMethod().getReturnType().isAnnotationPresent(Entity.class)) {
			String comment = pd.getReadMethod().getReturnType().getSimpleName() + " linked to the "
					+ javaType.getSimpleName();
			String s = getComment(comment,
					"the String representation of the " + pd.getReadMethod().getReturnType().getSimpleName())
					+ "\t@Value(\"#{T(com.octo.tools.crud.utils.StringUtils).toString(target."+f.getName()+")}\")\n";
			return s + "\tpublic String " + pd.getReadMethod().getName() + "();\n\n";
		}
		String comment = f.getName() + " of the " + javaType.getSimpleName();
		return getComment(comment, "the " + comment) + "\tpublic " + ReflectionUtils.getReturnTypeAsString(pd, f) + " "
				+ pd.getReadMethod().getName() + "();\n\n";
	}

	private String getComment(String comment, String returnStr) {
		return "\t/**\n\t * " + comment + "\n\t * @return " + returnStr + "\n\t */\n";
	}
}
