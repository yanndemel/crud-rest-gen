#**petclinic-api-audit-default** : Audit Rest API generation using default Hibernate Envers RevisionEntity

Use the crud-maven-plugin to generate Rest CRUD and audit APIs for all entities.

Build the sample
================
Run ``mvn clean install``

Run the sample
==============
Run ``mvn spring-boot:run``

Test the sample
=============
* Test the CRUD API : execute a GET request at the root of the API : ``$ curl http://localhost:8080/``.

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
* Test the audit API : execute a GET request at the root of the API : ``$ curl http://localhost:8080/history``.

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

For the API generation the dependencies are the same as for the [petclinic-api](../petclinic-api/README.md#project-dependencies) project, unless the petclinic-model which is replaced by [petclinic-audit-model-default](../petclinic-audit-model-default).
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
For compiling the audit generated classes, the dependency to [petclinic-audit-default](../petclinic-audit-default) must be added. It contains the custom abstract controller used by crud-maven-plugin for generating audit controllers. See the [crud-maven-plugin configuration](#crud-maven-plugin-configuration) section for details.
```xml
<!-- Custom AbstractAuditController and related classes -->
<dependency>
  <groupId>com.octo.tools.samples</groupId>
  <artifactId>petclinic-audit-default</artifactId>
  <version>0.0.1</version>
</dependency>
```
For testing the audit generated classes, the dependency to [crud-generator-utils-tests](../../crud-generator-utils-tests) must be added. It contains the base class ([AuditControllersTest](../../crud-generator-utils-tests/src/main/java/com/octo/tools/audit/AuditControllersTest.java)) used for testing the generated audit controllers.
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

###crud-maven-plugin configuration

###Generated sources
