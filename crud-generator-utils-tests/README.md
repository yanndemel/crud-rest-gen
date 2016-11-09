#**crud-generator-utils-tests** : Base test classes for automatic API documentation generation and audit controllers unit test

Java code
=========
Source code is divided into 3 packages :

###Audit controllers unit test

Package *com.octo.tools.audit* :
 * [**AuditControllersTest**](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils-tests/src/main/java/com/octo/tools/audit/AuditControllersTest.java) : Base class launching the unit tests of the generated audit API

###API documentation generation unit tests

Package *com.octo.tools.crud.doc* : 
 * [**ADocEntityGenerator**](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils-tests/src/main/java/com/octo/tools/crud/doc/ADocEntityGenerator.java) : Creation of the file ``target/generated-snippets/entities/allEntities.adoc`` based on the template [entity-api.adoc](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils-tests/src/main/resources/entity-api.adoc) applied on all JPA entities of the model
 * [**BaseApiDocumentation**](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils-tests/src/main/java/com/octo/tools/crud/doc/BaseApiDocumentation.java) : Generation of the documentation of the links present at the root of the API and of an error request
 * [**EntitiesApiDocumentation**](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils-tests/src/main/java/com/octo/tools/crud/doc/EntitiesApiDocumentation.java) : Generation of the documentation for each entity of the model : *list*, *create*, *get* and *update* requests, taken into account all constraints (annotated fields with javax.validation.constraints annotations) using a special [snippet](https://github.com/yanndemel/crud-rest-gen/blob/master/crud-generator-utils-tests/src/main/resources/org/springframework/restdocs/templates/request-fields.snippet)

###Utility classes : 
* *com.octo.tools.crud.util* : Utility classes used by the 4 previous classes during object model introspection

Resources files
==============
All resources files are used by the 3 tests in com.octo.tools.crud.doc for the API documentation generation.

* [api-guide.adoc](asciidocs/api-guide.adoc) : Template used by asciidoctor-maven-plugin for assembling the API documentation into a single file
* [request-fields.snippet](src/main/resources/org/springframework/restdocs/templates/request-fields.snippet) : Snippet used for documenting javax.validation.constraints present on the fields of the entities
* [entity-api.adoc](src/main/resources/entity-api.adoc) : Template by the EntitiesApiDocumentation test for documentating each entity

