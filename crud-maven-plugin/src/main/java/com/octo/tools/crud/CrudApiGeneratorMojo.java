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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Version;
import javax.persistence.metamodel.EntityType;

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
public class CrudApiGeneratorMojo extends AbstractGeneratorMojo {

	
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
	 * Name of the entities to exclude from Repository generation
	 */
	@Parameter(property = "excludedEntities", required = false)
	private List<String> excludedEntities;
	
	/**
	 * Set to false if you don't want Projections (*Excerpt.java source files) to be generated
	 */
	@Parameter(property = "projections", defaultValue = "true", required = false)
	private boolean projections;
	
	/**
	 * Set to false if you don't want Excerpts (automatically call projections) to be generated
	 */
	@Parameter(property = "excerpts", defaultValue = "false", required = false)
	private boolean excerpts;
	
	/**
	 * Set to false if you don't want to add generated sources to the compilation path
	 */
	@Parameter(property = "compile", defaultValue = "true", required = false)
	private boolean compile;
	
	public void execute() throws MojoExecutionException {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistentUnitName);
		EntityManager em = emf.createEntityManager();

		try {
			generateURLsConstants(em);
			if(projections) {
				List<String> classNames = generateProjectionExcepts(em);
				generateProjectionInit(em,classNames);
			}
			generateRepositories(em);
			if(compile)
				project.addCompileSourceRoot(outputDirectory);
		} catch (Exception e) {
			throw new MojoExecutionException("Exception during API generation", e);
		}

	}

	private void generateProjectionInit(EntityManager em, List<String> classNames) throws IOException {
		if(!classNames.isEmpty()) {
			String filename = "ProjectionConfig.java";
			File dir = new File(getRootDirectoryPath() + "/projection");
			if (!dir.exists())
				dir.mkdirs();
			Path path = Paths.get(dir.getPath(), filename);
			System.out.println("File " + path);
			StringBuilder proj = new StringBuilder(); 
			for(String name : classNames) {
				proj.append("\t\trestConfig.getProjectionConfiguration().addProjection(").append(name).append(".class);\n");				
			}
			
			BufferedWriter writer = Files.newBufferedWriter(path);
			InputStream in = getClass().getClassLoader().getResourceAsStream("ProjectionConfig.template");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			while (reader.ready()) {
				String s = reader.readLine();
				s = s.replaceAll("\\$\\$PACKAGE\\$\\$", packageName)
						.replaceAll("\\$\\$PROJECTIONS\\$\\$", proj.toString());
				writer.write(s + "\n");
			}
			reader.close();
			writer.close();
		}
		
	}

	/*
	 * Method used to generate Exceprt java source code in 
	 * ${packageName}.projection 
	 * 
	 */
	private List<String> generateProjectionExcepts(EntityManager em) throws Exception {
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		List<String> l = new ArrayList<>();
		for (EntityType<?> type : entityList) {
			Class<?> javaType = type.getJavaType();
			if (ReflectionUtils.isEntityExposed(javaType)) {
				String exerptName = javaType.getSimpleName() + "Excerpt";
				String filename = exerptName + ".java";
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
				String idClass = null;
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
					} else {
						idClass = f.getType().getSimpleName();
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
				System.out.println("projection : " + projection.toString());
				while (reader.ready()) {
					String s = reader.readLine();
					System.out.println("Line : " + s);
					s = s.replace("\\$\\$PACKAGE\\$\\$", packageName)
							.replace("\\$\\$ENTITY\\$\\$", javaType.getSimpleName())
							.replace("\\$\\$ENTITY_CLASS\\$\\$", javaType.getName())
							.replace("\\$\\$PROJECTION\\$\\$", projection.toString())
							.replace("\\$\\$ID_CLASS\\$\\$", idClass);
					writer.write(s + "\n");
				}
				l.add(exerptName);
				reader.close();
				writer.close();
			}
		}
		return l;
	}

	private void generateRepositories(EntityManager em) throws MojoExecutionException, IOException {
		File dir = getRootDirectory();

		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		for (EntityType<?> type : entityList) {
			Class<?> javaType = type.getJavaType();
			if (ReflectionUtils.isEntityExposed(javaType) && !isExcluded(javaType.getName())) {
				String filename = javaType.getSimpleName() + "Repository.java";
				Path path = Paths.get(dir.getPath(), filename);
				System.out.println("File " + path);
				BufferedWriter writer = Files.newBufferedWriter(path);
				InputStream in = getClass().getClassLoader().getResourceAsStream(excerpts ? "Repository.template" : "Repository.noProjection.template");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String idClass = ReflectionUtils.getIdClass(javaType);
				while (reader.ready()) {
					String s = reader.readLine();
					s = s.replaceAll("\\$\\$PACKAGE\\$\\$", packageName)
							.replaceAll("\\$\\$ENTITY\\$\\$", javaType.getSimpleName())
							.replaceAll("\\$\\$ENTITY_CLASS\\$\\$", javaType.getName())
							.replaceAll("\\$\\$ID_CLASS\\$\\$", idClass);
					writer.write(s + "\n");
				}
				reader.close();
				writer.close();
			}
		}
	}

	private boolean isExcluded(String name) {		
		return excludedEntities != null ? excludedEntities.contains(name) : false;
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
	private void generateURLsConstants(EntityManager em) throws Exception {
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();

		File dir = getRootDirectory();
		String filename = "URLs.java";
		Path path = Paths.get(dir.getPath(), filename);
		System.out.println("File " + path);
		try {
			BufferedWriter writer = Files.newBufferedWriter(path);
			writer.write("package "+packageName+";\n\n");
			writer.write("public interface URLs {\n\n");
			for (EntityType<?> type : entityList) {
				Class<?> javaType = type.getJavaType();
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

	private String getMethodAsString(PropertyDescriptor pd, Field f, Class<?> javaType) {
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
