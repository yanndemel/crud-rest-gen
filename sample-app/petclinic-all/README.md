#petclinic-all : CRUD Rest API, API documentation, Audit Rest API with custom RevisionEntity and CRUD Web application generation

Use the *crud-maven-plugin* to generate in single application containing :

* the CRUD Rest API
* the documentation of the API
* the CRUD Web Administration UI
* the Rest API for retrieving audit information and associated unit tests

This sample gathers into a single project the 3 samples :

* [petclinic-api-doc](../petclinic-api-doc)
* [petclinic-api-audit-custom](../petclinic-api-audit-custom)
* [petclinic-web](../petclinic-web)

Build the sample
================
Run ``mvn clean install``

Run the sample
==============
Run ``mvn spring-boot:run``

Test the sample
=============
* Test the API and the documentation : see [petclinic-api-doc](../petclinic-api-doc/README.md#test-the-sample) for details. **Warning!** the expected result of a GET at the root of the API is a little different from the petclinic-api-doc (adds the /history resource) :
```xml
{
  "_links" : {
    "petTypes" : {
      "href" : "http://localhost:8080/petTypes{?page,size,sort,projection}",
      "templated" : true
    },
    "vets" : {
      "href" : "http://localhost:8080/vets{?page,size,sort,projection}",
      "templated" : true
    },
    "pets" : {
      "href" : "http://localhost:8080/pets{?page,size,sort,projection}",
      "templated" : true
    },
    "specialties" : {
      "href" : "http://localhost:8080/specialties{?page,size,sort,projection}",
      "templated" : true
    },
    "visits" : {
      "href" : "http://localhost:8080/visits{?page,size,sort,projection}",
      "templated" : true
    },
    "owners" : {
      "href" : "http://localhost:8080/owners{?page,size,sort,projection}",
      "templated" : true
    },
    "doc" : {
      "href" : "http://localhost:8080/doc"
    },
    "history" : {
      "href" : "http://localhost:8080/history"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile"
    }
  }
}
```

* Test the audit Rest API : see [petclinic-api-audit-default](../petclinic-api-audit-default/README.md#test-the-sample) for details.
* Test the Web admin application : see [petclinic-web](../petclinic-web/README.md#test-the-web-application) for details.

How it works ?
============

The dependencies, Java code and crud-maven-plugin configuration are a combination of the 3 sample projects mentionned in the introduction :

* [petclinic-api-doc](../petclinic-api-doc/README.md#how-it-works-)
* [petclinic-api-audit-custom](../petclinic-api-audit-custom/README.md#how-it-works-)
* [petclinic-web](../petclinic-web/README.md#how-it-works-)

All details for each part of the generation process can be found in these 3 samples. The next sections will sum up the content of the project, but we won't get into many details at this stage (everything should be clear for you afer reading the doc of these 3 projects). 

Java code
========

* **Main** code : 1 class, the [*Application*](src/main/java/com/octo/tools/samples/petclinic/Application.java) launching the Spring Boot application.
```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.octo.tools.samples.petclinic.repository")
@EntityScan({"org.springframework.samples.petclinic.model"})	
@ComponentScan({"com.octo.tools.crud.admin", "com.octo.tools.crud.doc", "com.octo.tools.audit", 
	"com.octo.tools.samples.petclinic.repository.audit", 
	"com.octo.tools.samples.petclinic.repository.projection"})	
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {		
		return application.sources(Application.class);
	}
	
	public static void main(String[] args) {
		 new SpringApplicationBuilder(Application.class)         
         .run(args);
	}
}
```

* **Test** code : 4 classes :
 * [*PetClinicAuditControllerTest*](src\test\java\com\octo\tools\samples\petclinic\PetClinicAuditControllerTest.java) : launches the unit tests of the audit API (see  [petclinic-api-audit-default](../petclinic-api-audit-default/README.md#java-code) for details)
 * 3 classes in charge of generating the documentation of the API : see  [petclinic-api-doc](../petclinic-api-doc/README.md#java-code) for details

crud-maven-plugin configuration
===========================
```xml
<!-- Configuration of the crud-maven-plugin -->
<plugin>
	<groupId>com.octo.tools</groupId>
	<artifactId>crud-maven-plugin</artifactId>
	<version>0.0.2-SNAPSHOT</version>
	<configuration>
		<persistentUnitName>petclinic-model</persistentUnitName>
		<packageName>${packageName}</packageName>
		<restApiUrl>http://localhost:8080/</restApiUrl>		
		<auditControllerClassName>com.octo.tools.samples.AbstractAuditController</auditControllerClassName>
	</configuration>
	<executions>						
		<!-- CRUD API generation -->
		<execution>
			<id>api</id>
			<phase>generate-sources</phase>
			<goals>
				<goal>crudapi</goal>
			</goals>
		</execution>
		<!-- Audit controllers generation -->
		<execution>
			<id>audit</id>
			<phase>generate-sources</phase>
			<goals>
			    <goal>audit</goal>
			</goals>
		</execution>	
		<!-- CRUD Admin Web app generation -->
		<execution>
			<id>web</id>
			<phase>generate-resources</phase>
			<goals>
				<goal>crudweb</goal>
			</goals>
		</execution>
	</executions>
	<dependencies>
		<!-- Your JPA domain classes (must contain persistence.xml)  -->
		<dependency>
			<groupId>com.octo.tools.samples</groupId>
			<artifactId>petclinic-audit-model-custom</artifactId>
			<version>0.0.2-SNAPSHOT</version>
		</dependency>
		<!-- Custom AbstractAuditController and related classes -->
		<dependency>
			<groupId>com.octo.tools.samples</groupId>
			<artifactId>petclinic-audit-custom</artifactId>
			<version>0.0.2-SNAPSHOT</version>
		</dependency>						
		<!-- To avoid errors like Unable to load 'javax.el.ExpressionFactory'. 
			Check that you have the EL dependencies on the classpath, or use ParameterMessageInterpolator 
			instead -->
		<dependency>
			<groupId>javax.el</groupId>
			<artifactId>javax.el-api</artifactId>
			<version>2.2.4</version>
		</dependency>
	</dependencies>
</plugin>
```


> **Note** : the project is packaged as a "war" in order to be deployable in any servlet container like Tomcat
