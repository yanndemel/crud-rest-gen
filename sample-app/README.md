#**crud-maven-plugin - PetClinic samples**

Sample projects structure
=========================

The samples shows you the different ways of using the crud-maven-plugin.

### **petclinic-api** : Rest API generation

Depends on :

* *petclinic-model* (PetClinic JPA entities)


### **petclinic-api-doc** : Rest API & API documentation generation

Depends on :

* *petclinic-model* (PetClinic JPA entities)

### **petclinic-api-audit-default** : Audit Rest API generation using default Hibernate Envers RevisionEntity

Depends on :

* *petclinic-audit-model-default* (PetClinic JPA entities with @Audited annotations)
* *petclinic-audit-default* (Customization of the base audit controller used by generated audit classes)

### **petclinic-api-audit-custom** : Audit Rest API generation using a custom RevisionEntity

Depends on :

* *petclinic-audit-model-custom* (PetClinic JPA entities with @Audited annotations and a custom RevisionEntity )
* *petclinic-audit-custom* (Customization of the base audit controller used by generated audit classes)

### **petclinic-web** : CRUD Web application generation

Depends on :

* *petclinic-model* (PetClinic JPA entities)
* *petclinic-api* : Run only - the Rest API must be running (back-end of the Web app)

### **petclinic-all** : Rest API, API documentation, Audit Rest API with custom RevisionEntity and CRUD Web application generation

Depends on :

* *petclinic-audit-model-custom* (PetClinic JPA entities with @Audited annotations and a custom RevisionEntity )
* *petclinic-audit-custom* (Customization of the base audit controller used by generated audit classes)


Build the samples
=================
Run **``mvn clean install``** at the root of the ``sample-app`` folder to build all samples.

Run the samples
=============== 
The easiest way for running the samples is to go in the sample project root folder and execute the sample as a standard Spring Boot application using **``mvn spring-boot:run``**.

> **Note** : For running the ``petclinic-web`` sample you will have to execute the following comands : 
> 
> - ``mvn spring-boot:run`` for starting the API (at port 8080 by default)
> - ``mvn spring-boot:run -Dserver.port=<FREE_PORT>`` for starting the Web Admin UI where FREE_PORT has to be a free port (different from the one used by the API)


However you might prefer deploy the WARs in a Tomcat instance (or another servlet container) : that is naturally feasible.
