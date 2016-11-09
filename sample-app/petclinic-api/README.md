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


###Java code


###pom.xml configuration

###Generated sources

All generated sources are located in ```/target/generated-sources```.


@RepositoryRestResource 

Main dependencies
=================

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


* *[petclinic-model](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-model)* (PetClinic JPA entities)

pom.xml configuration
=====================
