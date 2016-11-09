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
