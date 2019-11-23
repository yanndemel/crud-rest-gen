package com.octo.tools.crud.doc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.octo.tools.common.AbstractCrudTest;
import com.octo.tools.crud.util.EntityInfo;

/*
 * 
 * To quickly generate all needed adoc files for entities
 * 
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ADocEntityGenerator extends AbstractCrudTest {

	public static final String TARGET_GENERATED_SNIPPETS = "target/generated-snippets";
	
	@Before
	public void setUp() throws ClassNotFoundException, IOException {	}
	
	@Test
	public void generateADocs() throws IOException, URISyntaxException, ClassNotFoundException {
		List<EntityInfo> entityInfoList = null;
		EntityManager em = emf.createEntityManager();
		try {
			entityInfoList = getEntityInfoList(em);
		} finally {
			em.close();
		}
		
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
	
	

}
