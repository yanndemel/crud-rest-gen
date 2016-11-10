#**petclinic-api-audit-default** : CRUD and Audit Rest APIs generation based on the auditable version of the Petclinic model

* default behaviour of the Hibernate Envers framework (org.hibernate.envers.DefaultRevisionEntity)
* crud-maven-plugin generates Rest CRUD and audit APIs (+unit tests) for all entities.

Build the sample
================
Run ``mvn clean install``

Run the sample
==============
Run ``mvn spring-boot:run``

Test the sample
=============
Test the **CRUD API** : execute a GET request at the root of the API : ``$ curl http://localhost:8080/``.
Server response :
```json
{
  "_links": {
    "petTypes": {
      "href": "http://localhost:8080/petTypes{?page,size,sort,projection}",
      "templated": true
    },
    "visits": {
      "href": "http://localhost:8080/visits{?page,size,sort,projection}",
      "templated": true
    },
    "specialties": {
      "href": "http://localhost:8080/specialties{?page,size,sort,projection}",
      "templated": true
    },
    "owners": {
      "href": "http://localhost:8080/owners{?page,size,sort,projection}",
      "templated": true
    },
    "pets": {
      "href": "http://localhost:8080/pets{?page,size,sort,projection}",
      "templated": true
    },
    "vets": {
      "href": "http://localhost:8080/vets{?page,size,sort,projection}",
      "templated": true
    },
    "history": {
      "href": "http://localhost:8080/history"
    },
    "profile": {
      "href": "http://localhost:8080/profile"
    }
  }
}
```
Test the **audit API** : execute a GET request at the root of the API : ``$ curl http://localhost:8080/history``.
Server response :
```json
{
  "_links": {
    "owner": {
      "href": "http://localhost:8080/history/owners"
    },
    "visit": {
      "href": "http://localhost:8080/history/visits"
    },
    "vet": {
      "href": "http://localhost:8080/history/vets"
    },
    "pettype": {
      "href": "http://localhost:8080/history/petTypes"
    },
    "specialty": {
      "href": "http://localhost:8080/history/specialties"
    },
    "pet": {
      "href": "http://localhost:8080/history/pets"
    }
  }
}
```

How it works ?
==============

###Project dependencies

For the **API generation** the dependencies are the same as for the [petclinic-api](../petclinic-api/README.md#project-dependencies) project, unless the petclinic-model which is replaced by [petclinic-audit-model-default](../petclinic-audit-model-default).
```xml
<!-- Your JPA domain classes (must contain persistence.xml)  -->
<dependency>
  <groupId>com.octo.tools.samples</groupId>
  <artifactId>petclinic-audit-model-default</artifactId>
  <version>0.0.1</version>
</dependency>						

<!-- Classes used by generated controllers -->
<dependency>
  <groupId>com.octo.tools</groupId>
  <artifactId>crud-generator-utils</artifactId>
  <version>0.0.1</version>
</dependency>
```
For **compiling** the **audit generated classes**, the dependency to [petclinic-audit-default](../petclinic-audit-default) must be added. It contains the custom abstract controller used by crud-maven-plugin for generating audit controllers. See the [crud-maven-plugin configuration](#crud-maven-plugin-configuration) section for details.
```xml
<!-- Custom AbstractAuditController and related classes -->
<dependency>
  <groupId>com.octo.tools.samples</groupId>
  <artifactId>petclinic-audit-default</artifactId>
  <version>0.0.1</version>
</dependency>
```
For **testing** the **audit generated classes**, the dependency to [crud-generator-utils-tests](../../crud-generator-utils-tests) must be added. It contains the base class ([AuditControllersTest](../../crud-generator-utils-tests/src/main/java/com/octo/tools/audit/AuditControllersTest.java)) used for testing the generated audit controllers.
```xml
<!-- Audit controllers test classes dependencies -->
<dependency>
    <groupId>com.octo.tools</groupId>
    <artifactId>crud-generator-utils-tests</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```

###Java code
The only Java class in the **main** code is the [Application](src/main/java/com/octo/tools/samples/petclinic/Application.java) class that initialize the Spring Boot context. The same annotations as in the [petclinic-api](../petclinic-api/README.md#java-code) sample are used. The only difference resides on the @componentScan annotation : *@ComponentScan({"com.octo.tools.crud.filter", "com.octo.tools.audit", "com.octo.tools.samples.petclinic.repository.audit"})* is used in petclinic-api-audit-default in order to enable, in addition to the CORS filter, the audit controllers ([AuditControllerBase](../../audit-core/src/main/java/com/octo/tools/audit/AuditControllerBase.java) and generated audit controllers) and the support of the ``/history`` Rest resource ([AuditResourceProcessor](../../audit-core/src/main/java/com/octo/tools/audit/AuditResourceProcessor.java)).

You will find in the **test** code one class ([PetClinicAuditControllerTest](src/test/java/com/octo/tools/samples/petclinic/PetClinicAuditControllerTest.java)) with empty body, extending the ([AuditControllersTest](../../crud-generator-utils-tests/src/main/java/com/octo/tools/audit/AuditControllersTest.java) class provided by [crud-generator-utils-tests](../../crud-generator-utils-tests) and annotated with ``@ContextConfiguration(classes = Application.class)`` in order to load the Spring context of the main application for the test.

###crud-maven-plugin configuration
The same configuration as in the [petclinic-api](../petclinic-api#crud-maven-plugin-configuration) project is used for the generation of the Rest API source code.
For the audit controllers the **audit** goal is used in this sample (bound to the generate-sources phase) :
```xml
<!-- Audit controllers generation -->
<execution>
    <id>audit</id>
    <phase>generate-sources</phase>
    <goals>
        <goal>audit</goal>
    </goals>
</execution>
```

* *crud-maven-plugin* needs the custom abstract audit controller as a plugin dependency in order to be able to load  [AbstractAuditController](../petclinic-audit-default/src/main/java/com/octo/tools/samples/AbstractAuditController.java), extended by all generated audit controllers.
```xml
<dependencies>
  <!-- Your JPA domain classes (must contain persistence.xml)  -->
  <dependency>
    <groupId>com.octo.tools.samples</groupId>
    <artifactId>petclinic-audit-model-default</artifactId>
    <version>0.0.1</version>
  </dependency>
  <!-- Custom AbstractAuditController and related classes -->
  <dependency>
    <groupId>com.octo.tools.samples</groupId>
    <artifactId>petclinic-audit-default</artifactId>
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
* The *auditControllerClassName* declared in the configuration of the plugin is located in petclinic-audit-default ( [AbstractAuditController](../petclinic-audit-default/src/main/java/com/octo/tools/samples/AbstractAuditController.java))
```xml
<configuration>
  <persistentUnitName>petclinic-model</persistentUnitName>
  <packageName>${packageName}</packageName>
  <auditControllerClassName>com.octo.tools.samples.AbstractAuditController</auditControllerClassName>
</configuration>
```

###Generated sources

For API sources generation details please see [here](../petclinic-api/README.MD#generated-sources).
Audit controllers are generated in ``target/generated-sources/`` in the package ``${packageName}.audit``. As for API generated sources you can choose to generate the source files without compiling them by setting the ``compile`` parameter to false in the crud-maven-plugin configuration.

> **Note** : the **auditControllerClassName** parameter is optional as shown in [pom-reflection.xml](pom-reflection.xml). If you don't define it :
> - you can remove petclinic-audit-default from your pom
> - crud-maven-plugin will generate audit controllers extending the default [AbstractDefaultAuditController](../../audit-core/src/main/java/com/octo/tools/audit/AbstractDefaultAuditController.java) and then use reflection for finding the @Id field on each entity.
> However to avoid reflection calls in the audit controllers, it is suitable to define a custom AbstractAuditController, extending [AbstractDefaultAuditController](../../audit-core/src/main/java/com/octo/tools/audit/AbstractDefaultAuditController.java) and overriding the ``protected Long getEntityId(T entity)`` method.
