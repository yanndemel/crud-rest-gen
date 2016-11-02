# crud-rest-gen
CRUD Application Generation based on the JPA entities of the model
==
__Provide the domain entities__ (standard JPA annotated classes), and let **crud-maven-plugin** generate :
* the CRUD Rest API
* the Documentation of the API
* the HTML5/AngularJS CRUD Administration GUI
* the Rest API for retrieving audit information if you use Hibernate Envers to audit your entities

Usage
-
Package your domain classes in a standalone maven project. Don't forget to place the persistence.xml file referencing your entity classes in a persistence unit.
Let call your-jpa-classes-1.0.jar the archive containing your entities.
You have to create a new maven project for the Rest API.


Technology stack :
* Spring Boot
* Spring Data Rest
* Spring Data JPA
* Spring Rest Docs
* Hibernate Envers
* Angular JS

Usage
