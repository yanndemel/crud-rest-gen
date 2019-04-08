package com.octo.tools.common;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;

import com.octo.tools.crud.util.EntityInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApiUrlTest extends AbstractCrudTest {	
	
	public static final Logger logger = LoggerFactory.getLogger(ApiUrlTest.class);

	@Before
	public void setUp() throws ClassNotFoundException, IOException {	}
	
	@Test
	public void processInUrls() throws IOException, ClassNotFoundException {
		String inFile = System.getProperty("inUrlsFile");
		String strCharset = System.getProperty("charset");
		Charset charset = strCharset != null ? Charset.forName(strCharset) : Charset.forName("ISO-8859-1");
		assertNotNull(inFile);
		Set<String> lines = new HashSet<>();
		this.entityInfoList = getEntityInfoList(em);
		for(EntityInfo info : entityInfoList) {
			String pluralName = info.getPluralName();
			lines.add(addFirstSlash(pluralName));
			lines.add(addFirstSlash(pluralName + "/{id}"));
			if(info.isSearch()) {
				lines.add(addFirstSlash(pluralName + "/search"));
			}
		}
		generateUrlsFile(inFile, charset, lines);		
	}

	public static void generateUrlsFile(String inFile, Charset charset, Set<String> lines)
			throws ClassNotFoundException, IOException {
		Set<BeanDefinition> classes = getBeanClasses();
		addLines(lines, classes);	
		ArrayList<String> list = new ArrayList<>(lines);
		Collections.sort(list);
		Files.write(Paths.get(inFile), list, charset,  StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void addLines(Set<String> lines, final Set<BeanDefinition> classes) throws ClassNotFoundException {
		// this is how you can load the class type from BeanDefinition instance
		for (BeanDefinition bean: classes) {
		    Class<?> clazz = Class.forName(bean.getBeanClassName());
		    RequestMapping pref = clazz.getAnnotation(RequestMapping.class);
		    if(pref != null) {
	    		logger.debug("Processing class {}", clazz.getSimpleName());
	    		Method[] declaredMethods = clazz.getDeclaredMethods();
	    		if(declaredMethods != null && declaredMethods.length > 0) {
	    			for(Method m : declaredMethods) {
	    				RequestMapping rm = m.getAnnotation(RequestMapping.class);
	    				if(rm != null) {
	    					if(rm.value() != null && rm.value().length > 0) {
	    						for(String path : rm.value())
	    							addUrl(lines, pref.value(), path);
	    					}
	    				}
	    			}
	    		}
	    		
		    	
		    }		    
		    
		}
	}

	public static Set<BeanDefinition> getBeanClasses() {
		// create scanner and disable default filters (that is the 'false' argument)
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		// add include filters which matches all the classes
		provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

		// get matching classes defined in the package
		final Set<BeanDefinition> classes = provider.findCandidateComponents(System.getProperty("packageName"));
		return classes;
	}

	public static void addUrl(Set<String> lines, String[] pref, String path) {
		if(pref == null || pref.length == 0) {
			lines.add(addFirstSlash(path));
		} else {
			for(String p : pref) {
				lines.add(concat(p, path));
			}	
		}		
	}

	public static String concat(String p, String path) {
		StringBuilder sb = new StringBuilder(addFirstSlash(p));
		if(p.endsWith("/")) {
			if(!path.startsWith("/")) {
				sb.append(path);
			} else {
				sb.append(path.substring(1));
			}
		} else {
			if(path.startsWith("/")) {
				sb.append(path);
			} else {
				sb.append("/").append(path);
			}
		}
		return sb.toString();
	}

	public static String addFirstSlash(String path) {
		return path.startsWith("/") ? path : "/" + path;
	}
	
	
}
