# petclinic-api-audit-custom : CRUD and Audit Rest APIs generation based on the auditable version of the Petclinic model (custom RevisionEntity)

* Custom behaviour of the Hibernate Envers framework (custom revision entity and listener defined in [petclinic-audit-custom](../petclinic-audit-custom))
* crud-maven-plugin generates Rest CRUD and audit APIs (+unit tests) for all entities.

To build, run and test the sample use the same procedure as for the [petclinic-api-audit-default](../petclinic-api-audit-default/README.md#build-the-sample) project.

How it works ?
==============

It works exactly the same as for the [petclinic-api-audit-default](../petclinic-api-audit-default/README.md#how-it-works-) project : just replace *default* by *custom* and you're done (I have made the replacement for you below). The last little difference is explained in the [notes](#generated-sources)

###Project dependencies

For the **API generation** the dependencies are the same as for the [petclinic-api](../petclinic-api/README.md#project-dependencies) project, unless the petclinic-model which is replaced by [petclinic-audit-model-custom](../petclinic-audit-model-custom).
```xml
<!-- Your JPA domain classes (must contain persistence.xml)  -->
<dependency>
  <groupId>com.octo.tools.samples</groupId>
  <artifactId>petclinic-audit-model-custom</artifactId>
  <version>1.0.0</version>
</dependency>						

<!-- Classes used by generated controllers -->
<dependency>
  <groupId>com.octo.tools</groupId>
  <artifactId>crud-generator-utils</artifactId>
  <version>1.0.0</version>
</dependency>
```
For **compiling** the **audit generated classes**, the dependency to [petclinic-audit-custom](../petclinic-audit-custom) must be added. It contains the custom abstract controller used by crud-maven-plugin for generating audit controllers and the custom resource support class for adding custom information returned by the audit API. See the [crud-maven-plugin configuration](#crud-maven-plugin-configuration) section for details.
```xml
<!-- Custom AbstractAuditController and related classes -->
<dependency>
  <groupId>com.octo.tools.samples</groupId>
  <artifactId>petclinic-audit-custom</artifactId>
  <version>1.0.0</version>
</dependency>
```
For **testing** the **audit generated classes**, the dependency to [crud-generator-utils-tests](../../crud-generator-utils-tests) must be added. It contains the base class ([AuditControllersTest](../../crud-generator-utils-tests/src/main/java/com/octo/tools/audit/AuditControllersTest.java)) used for testing the generated audit controllers.
```xml
<!-- Audit controllers test classes dependencies -->
<dependency>
    <groupId>com.octo.tools</groupId>
    <artifactId>crud-generator-utils-tests</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### Java code
The only Java class in the **main** code is the [Application](src/main/java/com/octo/tools/samples/petclinic/Application.java) class that initialize the Spring Boot context. The same annotations as in the [petclinic-api](../petclinic-api/README.md#java-code) sample are used. The only difference resides on the @componentScan annotation : *@ComponentScan({"com.octo.tools.crud.filter", "com.octo.tools.audit", "com.octo.tools.samples.petclinic.repository.audit"})* is used in petclinic-api-audit-custom in order to enable, in addition to the CORS filter, the audit controllers ([AuditControllerBase](../../audit-core/src/main/java/com/octo/tools/audit/AuditControllerBase.java) and generated audit controllers) and the support of the ``/history`` Rest resource ([AuditResourceProcessor](../../audit-core/src/main/java/com/octo/tools/audit/AuditResourceProcessor.java)).

You will find in the **test** code one class ([PetClinicAuditControllerTest](src/test/java/com/octo/tools/samples/petclinic/PetClinicAuditControllerTest.java)) with empty body, extending the ([AuditControllersTest](../../crud-generator-utils-tests/src/main/java/com/octo/tools/audit/AuditControllersTest.java) class provided by [crud-generator-utils-tests](../../crud-generator-utils-tests) and annotated with ``@ContextConfiguration(classes = Application.class)`` in order to load the Spring context of the main application for the test.

### crud-maven-plugin configuration
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

* *crud-maven-plugin* needs the custom abstract audit controller as a plugin dependency in order to be able to load  [AbstractAuditController](../petclinic-audit-custom/src/main/java/com/octo/tools/samples/AbstractAuditController.java), extended by all generated audit controllers.
```xml
<dependencies>
  <!-- Your JPA domain classes (must contain persistence.xml)  -->
  <dependency>
    <groupId>com.octo.tools.samples</groupId>
    <artifactId>petclinic-audit-model-custom</artifactId>
    <version>1.0.0</version>
  </dependency>
  <!-- Custom AbstractAuditController and related classes -->
  <dependency>
    <groupId>com.octo.tools.samples</groupId>
    <artifactId>petclinic-audit-custom</artifactId>
    <version>1.0.0</version>
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
* *crud-maven-plugin* uses the *auditControllerClassName* declared in the configuration and located in [petclinic-audit-custom](../petclinic-audit-custom) : all generated audit controllers will extend this class.
```xml
<configuration>
  <persistentUnitName>petclinic-model</persistentUnitName>
  <packageName>${packageName}</packageName>
  <auditControllerClassName>com.octo.tools.samples.AbstractAuditController</auditControllerClassName>
</configuration>
```

### Generated sources

For API sources generation details please see [here](../petclinic-api/README.md#generated-sources).
Audit controllers are generated in ``target/generated-sources/`` in the package ``${packageName}.audit``. As for API generated sources you can choose to generate the source files without compiling them by setting the ``compile`` parameter to false in the crud-maven-plugin configuration.

> **Note** : the **auditControllerClassName** parameter is optional as shown for the [petclinic-api-audit-default](../petclinic-api-audit-default/README.md#generated-sources) project. If you don't define it :
>
>- you can remove petclinic-audit-custom from your pom
>- crud-maven-plugin will generate audit controllers extending the default [AbstractReflectionAuditController](../../audit-core/src/main/java/com/octo/tools/audit/AbstractReflectionAuditController.java) and then use reflection for finding the id of each entity and the revision number and timestamp of each revision entity.
>
> However to avoid reflection calls in the audit controllers, it is suitable to define a custom AbstractAuditController, extending [``AbstractAuditController<T, R>``](../../audit-core/src/main/java/com/octo/tools/audit/AbstractAuditController.java) and overriding the following methods :
> 
>- ``protected Long getEntityId(T entity)``
>- ``protected AuditResourceSupport<T> newAuditResourceSupport(RevisionType revType, T entity, CustomRevisionEntity revEntity)``
>- ``protected Long getRevisionEntityId(CustomRevisionEntity revEntity)``
>- ``protected Long getRevisionEntityTimestamp(CustomRevisionEntity revEntity)``
