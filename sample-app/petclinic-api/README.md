#**petclinic-api** : API generation

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

###Dependencies

* The first dependency is petclinic-model, the data model. The original petclinic model has been slightly modified to fit crud-maven-plugin [pre-requisites](https://github.com/yanndemel/crud-rest-gen/blob/master/README.md#pre-requisites). Please look at [petclinic-model](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-model) for details.
* The second dependency is crud-generator-utils that contains utility classes used by the generated Projection classes (StringUtils.toString method) and by the application (CORS filter for enabling requests from any host, mandatory for using the 
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


###pom.xml configuration

###Generated sources

All generated sources are located in ```/target/generated-sources```.
The base package for @RepositoryRestResource classes is the value of the ``packageName`` parameter in the crud-maven-plugin configuration.
The base package for Projection classes (\*Excerpt.java files) is the value of the ``packageName`` parameter suffixed by ``.projection``.
For each JPA entity, a @RepositoryRestResource source file and a Projection are used by the generated Web Admin UI

* @RepositoryRestResource 
* Projection classes

In the sample the generated source code is compiled and included in the packaged application. However you can choose, by setting the ``compile`` parameter to ``false`` in the crud-maven-plugin configuration, to use the crud-maven-plugin only for source code generation (e.g. if you want to customize the generated sources after generation and include them "manually" in your project).

Main dependencies
=================


* *[petclinic-model](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-model)* (PetClinic JPA entities)

pom.xml configuration
=====================
