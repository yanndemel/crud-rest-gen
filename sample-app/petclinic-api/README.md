#**petclinic-api** : Rest API generation

Use the crud-maven-plugin to generate the Rest API for all entities.

Build the sample
================
Run ``mvn clean install``

Run the sample
==============
Run ``mvn spring-boot:run``

Test the API
=============
Execute a GET request at the root of the API : ``$ curl http://localhost:8080/``.

Server response : 
```json
{
  "_links": {
    "pets": {
      "href": "http://localhost:8080/pets{?page,size,sort,projection}",
      "templated": true
    },
    "owners": {
      "href": "http://localhost:8080/owners{?page,size,sort,projection}",
      "templated": true
    },
    "specialties": {
      "href": "http://localhost:8080/specialties{?page,size,sort,projection}",
      "templated": true
    },
    "visits": {
      "href": "http://localhost:8080/visits{?page,size,sort,projection}",
      "templated": true
    },
    "petTypes": {
      "href": "http://localhost:8080/petTypes{?page,size,sort,projection}",
      "templated": true
    },
    "vets": {
      "href": "http://localhost:8080/vets{?page,size,sort,projection}",
      "templated": true
    },
    "profile": {
      "href": "http://localhost:8080/profile"
    }
  }
}
```

How it works ?
==============

###Project dependencies

In addition to the standard Spring Boot dependencies (spring-boot-starter-data-rest, spring-boot-starter-data-jpa and spring-boot-starter-tomcat) and the H2 database, two dependencies are needed for building the project :

* The first is *petclinic-model*, the data model. The original petclinic model has been slightly modified to fit crud-maven-plugin [pre-requisites](https://github.com/yanndemel/crud-rest-gen/blob/master/README.md#pre-requisites). Please look at [petclinic-model](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-model) for details.
* The second is *crud-generator-utils* that contains utility classes used by the generated Projection classes ([StringUtils](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils/src/main/java/com/octo/tools/crud/utils/StringUtils.java).toString method) and by the application ([CORS filter](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils/src/main/java/com/octo/tools/crud/filter/SimpleCORSFilter.java) for enabling requests from any host, mandatory for using the API from another server and in particular for the sample application [petclinic-web](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-web)).

Extract from pom.xml :
```xml
<!-- Your JPA domain classes (must contain persistence.xml)  -->
<dependency>
	<groupId>com.octo.tools.samples</groupId>
	<artifactId>petclinic-model</artifactId>
	<version>0.0.1</version>
</dependency>						

<!-- Classes used by generated controllers -->
<dependency>
	<groupId>com.octo.tools</groupId>
	<artifactId>crud-generator-utils</artifactId>
	<version>0.0.1</version>
</dependency>
```
###Java code

The only Java class is the [Application](https://github.com/yanndemel/crud-rest-gen/blob/master/sample-app/petclinic-api/src/main/java/com/octo/tools/samples/petclinic/Application.java) class that initialize the Spring Boot context. Following annotations are used :

* *@SpringBootApplication* to enable Spring Boot context
* *@EnableJpaRepositories(basePackages = "com.octo.tools.samples.petclinic.repository")* to let Spring Boot find the @RepositoryRestResource classes
* *@EntityScan({"org.springframework.samples.petclinic.model"})* to let Spring Boot find the JPA entities
* *@ComponentScan({"com.octo.tools.crud.filter"})* to enable the CORS filter

No application.properties file is used in this sample. Therefore the default Spring Boot h2 in-memory database is used.

###crud-maven-plugin configuration

It is located in the ``<pluginManagement>`` section of the [pom.xml](https://github.com/yanndemel/crud-rest-gen/blob/master/sample-app/petclinic-api/pom.xml).
The **crudapi** goal is used in this sample  (bound to the generate-sources phase).
```xml					
<!-- CRUD API generation -->
<execution>
	<id>api</id>
	<phase>generate-sources</phase>
	<goals>
		<goal>crudapi</goal>
	</goals>
</execution>						
```

* *crud-maven-plugin* needs the domain classes with the associated persistence.xml as a plugin dependency in order to be able to load the EntityManagerFactory :
```xml
<dependencies>
    <!-- Your JPA domain classes (must contain persistence.xml)  -->
    <dependency>
        <groupId>com.octo.tools.samples</groupId>
        <artifactId>petclinic-model</artifactId>
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
```

* The *persistentUnitName* declared in the configuration of the plugin :
 ``<persistentUnitName>petclinic-model</persistentUnitName>``
must match name of the *persistence-unit* declared in the [*persistence.xml*](https://github.com/yanndemel/crud-rest-gen/blob/master/sample-app/petclinic-model/src/main/resources/META-INF/persistence.xml) : 
``<persistence-unit name="petclinic-model">``
* The *packageName* declared in the configuration of the plugin is the base package for @RepositoryRestResource generated classes. The base package for @Projection generated classes is the value of the *packageName* parameter suffixed by ``.projection`` :
```xml
<configuration>
    <persistentUnitName>petclinic-model</persistentUnitName>
    <packageName>${packageName}</packageName>
</configuration>
```

###Generated sources

For each JPA entity, crud-maven-plugin:crudapi generates :

* One @RepositoryRestResource source file implementing ``PagingAndSortingRepository<ENTITY, Long>`` in the package ``${packageName}``
* One @Projection source file (\*Excerpt.java file) in the package ``${packageName}.projection`` : the generation of this file can be disabled by setting the parameter ``projections`` to false in the plugin configuration. The aim of the projection is to give a "flat" view of each entity by returning a String representation of each linked entity (used by the generated admin web app, cf. [petclinic-web](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-web)).

In addition crud-maven-plugin generates the URL class in the package ``${packageName}`` which gathers all URLs used in the Rest API mapping.

All generated sources are located in ```/target/generated-sources```.

In the sample, the generated source code is compiled and included in the packaged application. However you can choose, by setting the ``compile`` parameter to ``false`` in the crud-maven-plugin configuration, to use the crud-maven-plugin only for source code generation (e.g. if you want to customize the generated sources after generation and include them "manually" in your project).



> **Note** : the project is packaged as a "war" in order to be deployable in any servlet container like Tomcat
