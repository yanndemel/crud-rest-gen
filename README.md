# crud-rest-gen
CRUD Application Generation based on the JPA entities of the model
==
__Provide the domain entities__ (standard JPA annotated classes), and let **crud-maven-plugin** generate :
* the CRUD Rest API
* the Documentation of the API
* the HTML5/AngularJS CRUD Administration UI
* the Rest API for retrieving audit information if you use Hibernate Envers to audit your entities

Usage
-
Package your domain classes in a standalone maven project. Don't forget to place the persistence.xml file referencing your entity classes in a persistence unit.
Let call your-jpa-classes-1.0.jar the archive containing your entities.
You have to create a new maven project for the Rest API. You can use the same project for the Web administration UI or you can create a separate maven project. In the latter case you will have to provide in the pom.xml the root URL of the API.
In this first example we use the same maven project to generate the Rest API (+its documentation), and the Web administration UI.

```xml
<project>
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.nexity.refdata</groupId>
	<artifactId>refdata-web</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>war</packaging>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.4.1.RELEASE</version>
	</parent>

	<name>refdata-web</name>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<compile.version>1.8</compile.version>
		<snippetsDirectory>${project.build.directory}/generated-snippets</snippetsDirectory>
		<packageName>com.nexity.refdata.repository</packageName>
	</properties>


	<dependencies>
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

		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
		</dependency>
		
		<dependency>
			<groupId>com.nexity.refdata</groupId>
			<artifactId>refdata-model</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		
		<!-- Custom AbstractAuditController and related classes -->
		<dependency>
			<groupId>com.nexity.refdata</groupId>
			<artifactId>refdata-audit</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		
		<!-- Audit classes used by generated audit controllers -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>audit-core</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>

		<!-- Classes used by generated controllers -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>crud-generator-utils</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>

		<!-- Start dependencies for API documentation generation -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>crud-generator-utils-tests</artifactId>
			<version>0.0.1-SNAPSHOT</version>
			<scope>test</scope>
		</dependency>
		
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-envers</artifactId>
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


		<finalName>refdataweb-${project.version}</finalName>
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
									<version>0.0.1-SNAPSHOT</version>
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
					<version>0.0.1-SNAPSHOT</version>
					<configuration>
						<persistentUnitName>refdata-model</persistentUnitName>
						<restApiUrl>${restApiUrl}</restApiUrl>
						<packageName>${packageName}</packageName>
						<auditControllerClassName>com.nexity.refdata.audit.AbstractAuditController</auditControllerClassName>
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
<project>
	<modelVersion>4.0.0</modelVersion>

	<groupId>your-groupId</groupId>
	<artifactId>your-artifactId</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>war</packaging>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.4.1.RELEASE</version>
	</parent>

	<name>your-project-web</name>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<compile.version>1.8</compile.version>
		<snippetsDirectory>${project.build.directory}/generated-snippets</snippetsDirectory>
		<packageName>your-groupId.repository</packageName>
	</properties>


	<dependencies>
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
		
		<dependency>
			<groupId>your-groupId</groupId>
			<artifactId>your-project-model</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		
		<!-- Custom AbstractAuditController and related classes -->
		<dependency>
			<groupId>your-groupId</groupId>
			<artifactId>your-project-audit</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		
		<!-- Audit classes used by generated audit controllers -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>audit-core</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>

		<!-- Classes used by generated controllers -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>crud-generator-utils</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>

		<!-- Start dependencies for API documentation generation -->
		<dependency>
			<groupId>com.octo.tools</groupId>
			<artifactId>crud-generator-utils-tests</artifactId>
			<version>0.0.1-SNAPSHOT</version>
			<scope>test</scope>
		</dependency>
		
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-envers</artifactId>
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


		<finalName>your-projectweb-${project.version}</finalName>
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
									<version>0.0.1-SNAPSHOT</version>
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
					<version>0.0.1-SNAPSHOT</version>
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
						<!-- JPA model (must contain persistence.xml) -->
						<dependency>
							<groupId>your-groupId</groupId>
							<artifactId>your-project-model</artifactId>
							<version>0.0.1-SNAPSHOT</version>
						</dependency>
						<!-- Custom AbstractAuditController -->
						<dependency>
							<groupId>your-groupId</groupId>
							<artifactId>your-project-audit</artifactId>
							<version>0.0.1-SNAPSHOT</version>
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
</project>```

Technology stack :
* Spring Boot
* Spring Data Rest
* Spring Data JPA
* Spring Rest Docs
* Hibernate Envers
* Angular JS

Usage
