package com.octo.tools.crud.doc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.octo.tools.crud.util.EntityInfo;
import com.octo.tools.crud.utils.ReflectionUtils;

/*
 * 
 * To quickly generate all needed adoc files for entities
 * 
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ADocEntityGenerator {

	public static final String TARGET_GENERATED_SNIPPETS = "target/generated-snippets";
	
	@Autowired
	protected EntityManager em;	
	
	@Test
	public void generateADocs() throws IOException, URISyntaxException, ClassNotFoundException {
		List<EntityInfo> entityInfoList = getEntityInfoList(em);
		List<String> files = new ArrayList<String>();
		for(EntityInfo info : entityInfoList) {
			String filename = info.getSimpleName() + ".adoc";
			files.add(filename);
			File dir = new File(TARGET_GENERATED_SNIPPETS + "/entities/");
			dir.mkdirs();
			Path path = Paths.get(dir.getPath(), filename);
			System.out.println("File " + path);
			BufferedWriter writer = Files.newBufferedWriter(path);
			
			InputStream in = getClass().getClassLoader().getResourceAsStream("entity-api.adoc");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			while (reader.ready()) {
				String s = reader.readLine();
				s = s.replaceAll("\\$\\{entity\\}", info.getSimpleName()).replaceAll("\\$\\{entities\\}", info.getPluralName())
						.replaceAll("\\$\\{Entity\\}", info.getSimpleName1stUpper()).replaceAll("\\$\\{Entities\\}", info.getPluralName1stUpper());
				writer.write(s + "\n");
			}
			reader.close();
			writer.close();			
		}
		Path path = Paths.get(TARGET_GENERATED_SNIPPETS + "/entities/allEntities.adoc");
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		for(String s : files) {		
			writer.write("\n\n\ninclude::{snippets}/entities/"+s+"[]\n");
		}
		writer.close();
	}
	
	

	public static List<EntityInfo> getEntityInfoList(EntityManager em) throws ClassNotFoundException {
		Set<EntityType<?>> entityList = em.getMetamodel().getEntities();
		List<EntityInfo> list = new ArrayList<EntityInfo>();
		for (EntityType type : entityList) {
			Class javaType = type.getJavaType();
			if (ReflectionUtils.isEntityExposed(javaType)) {
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
				info.setSearch(hasSearch(javaType));
				info.setPaged(isPaged(javaType));
				list.add(info);
			}
		}
		Collections.sort(list, (p1, p2) -> p1.getSimpleName().compareTo(p2.getSimpleName()));
		return list;
	}

	private static boolean isPaged(Class javaType) throws ClassNotFoundException {		
		return PagingAndSortingRepository.class.isAssignableFrom(getRepository(javaType));
	}

	private static boolean hasSearch(Class javaType) throws ClassNotFoundException {
		Class repoClass = getRepository(javaType);
		Method[] methods = repoClass.getDeclaredMethods();
		for(Method m : methods) {
			if(m.getName().startsWith("findBy"))
				return true;
		}
		return false;
	}

	private static Class getRepository(Class javaType) throws ClassNotFoundException {
		return Class.forName(System.getProperty("packageName") + "." + javaType.getSimpleName()+"Repository");
	}

	static String getName1stLower(String name) {
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	static String getPluralName(String name) {
		String entities1 = name.endsWith("y") ? name.substring(0, name.length() - 1) + "ies"
				: name.endsWith("s") ? name + "es" : name + "s";
		return entities1;
	}

}
