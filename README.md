
#**crud-maven-plugin**

*CRUD Application Generation from JPA entities*
=============

Provide the domain entities (standard JPA annotated classes), and let crud-maven-plugin generate for you :

* the **CRUD Rest API**
* the **documentation** of the API
* the HTML5/AngularJS **CRUD Administration UI**
* the **Rest API** for retrieving **audit information** and associated unit tests if you use Hibernate Envers to audit your entities

> **Technology stack**

> - Spring Boot
> - Spring Data Rest
> - Spring Data JPA
> - Spring Rest Docs
> - Hibernate Envers
> - Angular JS

Install the plugin
------------------
Authorized users can use the **OCTO Nexus server** for managing crud-maven-plugin dependencies.

Or you can build and install/deploy all necessary components in you local repo / Nexus server by invoking ``mvn clean install`` / ``mvn clean deploy``  at the root of crud-rest-gen project. This will install all necessary artifacts :
* *audit-core* : Base classes used by generated audit controllers
* *crud-generator-utils* : Utility classes for Reflection & String operations and Controllers for accessing generated documentation page and history API
* *crud-generator-utils-tests* : Base classes to be extended in the API project for API documentation generation and generated audit controllers testing
* *crud-web-generator* : Classes used by crud-maven-plugin to generate the CRUD Web administration UI (relying on the generated Rest API)
* **crud-maven-plugin** : Mojos for CRUD Rest API generation (generate-sources phase), audit controllers generation (generate-sources phase), CRUD Web app generation (generate-resources phase)
* The samples located in the sample-app directory

Browse the samples
----------------------------
The [Spring Petclinic](https://github.com/spring-projects/spring-petclinic) model is used for all samples. It has been slightly adapted to fit the pre-requisites. The samples will learn you how to generate, based on the domain classes :

* the Rest API
* the documentation of the API
* the Rest API for audited entities and associated unit tests
* the Web application for administrating the data model

All details can be found in the [README.md](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/README.md) of the ``sample-app`` directory.

Use your own data model
--------------------
###Pre-requisites

 1. Your entities must be simple POJOs annotated with standard javax.persistence annotations
 2. The type of **all @Id** in your entities **must be java.lang.Long**
 3. If you generate CRUD Web Administration with crud-maven-plugin:**crudweb** all entities must have/inherit a **public String getShortLabel()** method (returning the description of the entity).  

###Project setup

 1. Package the **persistence.xml** file referencing your entity classes in a maven artifact containing (or dependent from) your JPA entities.
 2. You have to create a new maven project for the Rest API. You can use the same project for the Web administration UI or you can create a separate maven project.

> **Note** : It is **strongly recommended** to **browse the samples before starting** with your own model and choose the kind of project you need (API only ? Documentation ? Audit API ? Web application ? Packaging mode : all-in-one or separate applications ?).

You can find below a sample *pom.xml* for **all-in-one** generation (more details can be found in the [petclinic-all sample](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-all/README.md)) : same maven project to generate the Rest API (+its documentation), the audit Rest API (+associated unit tests) and the Web administration UI :
```xml
<project>
	<modelVersion>4.0.0</modelVersion>

	<groupId>your-groupId</groupId>
	<artifactId>your-artifactId</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<!-- WAR if you intend to deploy in an external servlet container like tomcat -->
	<packaging>war</packaging>
	<!-- The project has to be a Spring boot project -->
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.4.1.RELEASE</version>
	</parent>
	<name>your-project-web</name>
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<compile.version>1.8</compile.version>
		<!-- The Snippets directory used by Spring Rest Docs (asciidoctor maven plugin) -->
		<snippetsDirectory>${project.build.directory}/generated-snippets</snippetsDirectory>
		<!-- The package name of the generated RepositoryRestResource classes -->
		<packageName>your-repository-classes-package</packageName>
	</properties>
	<dependencies>
		<!-- Spring dependencies -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-rest</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
			<scope>provided</scope>
		</dependency>

		<!-- Change with your favorite DB -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
		</dependency>
		
		<!-- Your JPA domain classes (must contain persistence.xml)  -->
		<dependency>
			<groupId>your-groupId</groupId>
			<artifactId>your-project-model</artifactId>
			<version>0.0.1</version>
		</dependency>
		
		<!-- Custom AbstractAuditController and related classes -->
		<dependency>
			<groupId>your-groupId</groupId>
			<artifactId>your-project-audit</artifactId>
			<version>0.0.1</version>
		</dependency>
		
		<!-- Audit classes used by generated audit controllers -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>audit-core</artifactId>
			<version>0.0.1</version>
		</dependency>
		
		<!-- Audit management using Envers -->
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-envers</artifactId>
		</dependency>
		
		<!-- Classes used by generated controllers -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>crud-generator-utils</artifactId>
			<version>0.0.1</version>
		</dependency>

		<!-- Start dependencies for API documentation generation -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>crud-generator-utils-tests</artifactId>
			<version>0.0.1</version>
			<scope>test</scope>
		</dependency>				
		<dependency>
			<groupId>joda-time</groupId>
			<artifactId>joda-time</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.restdocs</groupId>
			<artifactId>spring-restdocs-mockmvc</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-test</artifactId>
			<scope>test</scope>
		</dependency>
		<!-- End of dependencies for API documentation generation -->
		
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>		
		<plugins>
			<plugin>
				<groupId>com.octo.tools</groupId>
				<artifactId>crud-maven-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>${compile.version}</source>
					<target>${compile.version}</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-war-plugin</artifactId>
				<configuration>
					<failOnMissingWebXml>false</failOnMissingWebXml>
				</configuration>
			</plugin>
			<!-- Unpacking crud-generator-utils-tests jar so that asciidoctor-maven-plugin can locate the .adoc template in the sourceDirectory -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<executions>
					<execution>
						<id>unpack</id>
						<phase>generate-test-sources</phase>
						<goals>
							<goal>unpack</goal>
						</goals>
						<configuration>
							<artifactItems>
								<artifactItem>
									<groupId>com.octo.tools</groupId>
									<artifactId>crud-generator-utils-tests</artifactId>
									<version>0.0.1</version>
									<type>jar</type>
									<overWrite>true</overWrite>
									<outputDirectory>${project.build.directory}/crud-tests</outputDirectory>
								</artifactItem>
							</artifactItems>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<!-- Tests execution : you just have to extend existing test classes located in crud-generator-utils-tests -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<includes>
						<!-- Test for generated AuditControllers -->
						<include>**/*Test.java</include>
						<!-- Tests generating the API documentation -->
						<include>**/*Documentation.java</include>
						<include>**/*EntityGenerator.java</include>
					</includes>					
					<systemPropertyVariables>
						<!-- Used by BaseApiDocumentation : package name of the repository classes -->
						<packageName>${packageName}</packageName>
						<!-- Used by BaseApiDocumentation : set to true if you have generated audit controllers with crud-maven-plugin -->
						<audit>true</audit>
						<!-- Used by BaseApiDocumentation : set to true if you add "com.octo.tools" to the @ComponentScan annotation on your @SpringBootApplication class -->
						<doc>true</doc>
						<!-- Used by EntitiesApiDocumentation : set to true if all the JPA entities (unless RevisionEntity) have/inherit a public getShortLabel method -->
                   <shortLabel>true</shortLabel>
					</systemPropertyVariables>
				</configuration>
			</plugin>
			<!-- Generation of the API documentation -->
			<plugin>
				<groupId>org.asciidoctor</groupId>
				<artifactId>asciidoctor-maven-plugin</artifactId>
				<version>1.5.2</version>
				<executions>
					<execution>
						<id>generate-docs</id>
						<phase>prepare-package</phase>
						<goals>
							<goal>process-asciidoc</goal>
						</goals>
						<configuration>
							<!-- Template located in the unpacked test dependency crud-generator-utils-tests -->
							<sourceDirectory>${project.build.directory}/crud-tests/asciidoc</sourceDirectory>
							<backend>html</backend>
							<doctype>book</doctype>
							<attributes>
								<snippets>${snippetsDirectory}</snippets>
							</attributes>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<!-- Copy of the generated documentation to static/docs/api -->
			<plugin>
				<artifactId>maven-resources-plugin</artifactId>
				<executions>
					<execution>
						<id>copy-resources-doc</id>
						<phase>prepare-package</phase>
						<goals>
							<goal>copy-resources</goal>
						</goals>
						<configuration>
							<outputDirectory>
								${project.build.outputDirectory}/static/docs/api
							</outputDirectory>
							<resources>
								<resource>
									<directory>
										${project.build.directory}/generated-docs
									</directory>
								</resource>
							</resources>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
		<pluginManagement>
			<plugins>
				<!-- Configuration of the crud-maven-plugin -->
				<plugin>
					<groupId>com.octo.tools</groupId>
					<artifactId>crud-maven-plugin</artifactId>
					<version>0.0.1</version>
					<configuration>
						<persistentUnitName>your-project-model</persistentUnitName>
						<restApiUrl>${restApiUrl}</restApiUrl>
						<packageName>${packageName}</packageName>
						<auditControllerClassName>your-groupId.audit.AbstractAuditController</auditControllerClassName>
					</configuration>
					<executions>
						<!-- CRUD Admin Web app generation -->
						<execution>
							<id>web</id>
							<phase>generate-resources</phase>
							<goals>
								<goal>crudweb</goal>
							</goals>
						</execution>
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
					</executions>
					<dependencies>
						<!-- Your JPA domain classes (must contain persistence.xml) -->
						<dependency>
							<groupId>your-groupId</groupId>
							<artifactId>your-project-model</artifactId>
							<version>0.0.1</version>
						</dependency>
						<!-- Custom AbstractAuditController -->
						<dependency>
							<groupId>your-groupId</groupId>
							<artifactId>your-project-audit</artifactId>
							<version>0.0.1</version>
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
			</plugins>
		</pluginManagement>
	</build>
	<profiles>
		<profile>
			<activation>
				<activeByDefault>true</activeByDefault>
				<property>
					<name>!restApiUrl</name>
				</property>
			</activation>
			<properties>
				<restApiUrl>http://localhost:8080/api/</restApiUrl>
			</properties>
		</profile>
	</profiles>
</project>
```

The data model artefact must contain a **persistence.xml** file as shown below :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.1" xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">
	<persistence-unit name="your-persistence-unit-name">
		<class>Your-Entites-Here</class>
		...			
		<properties>
			<property name="hibernate.archive.autodetection" value="class" />
			<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect" />
			<property name="hibernate.connection.driver_class" value="org.h2.Driver" />
			<property name="hibernate.connection.url" value="jdbc:h2:mem:AZ;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS HISTORY" />
			<property name="hibernate.connection.user" value="sa" />
			<property name="hibernate.flushMode" value="FLUSH_AUTO" />
			<property name="hibernate.hbm2ddl.auto" value="update" />
		</properties>
	</persistence-unit>
</persistence>
```

For an all-in-one application, the Application main class must contain the following annotations :

```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "value of ${packageName}")
@EntityScan({"package(s) of your entities"})	
@ComponentScan({"com.octo.tools.crud.admin", "com.octo.tools.crud.doc", "com.octo.tools.audit", 
	"${packageName}.audit", 
	"${packageName}.projection"})	
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
