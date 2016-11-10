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

* Test the audit Rest API : see [petclinic-api-audit-custom](../petclinic-api-audit-custom/README.md#test-the-sample) for details.
* Test the Web admin application : see [petclinic-web](../petclinic-web/README.md#test-the-sample) for details.
