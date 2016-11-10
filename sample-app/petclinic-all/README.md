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
* Test the API and the documentation (see [petclinic-api-doc](../petclinic-api-doc/README.md#test-the-sample) for details). **Warning!** the expected result of a GET at the root of the API is a little different from the petclinic-api-doc (adds the /history resource) :


