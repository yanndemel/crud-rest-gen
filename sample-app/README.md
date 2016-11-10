#**crud-maven-plugin - PetClinic samples**

Sample projects structure
=========================

The samples shows you the different ways of using the crud-maven-plugin.

### **[petclinic-api](petclinic-api)** : Rest API generation

### **[petclinic-api-doc](petclinic-api-doc)** : Rest API & API documentation generation

### **[petclinic-api-audit-default](petclinic-api-audit-default)** : CRUD and Audit Rest APIs generation based on the auditable version of the Petclinic model

### **[petclinic-api-audit-custom](petclinic-api-audit-custom)** : CRUD and Audit Rest APIs generation based on the auditable version of the Petclinic model (custom RevisionEntity)

### **[petclinic-web](petclinic-web)** : CRUD Web application generation

### **[petclinic-all](petclinic-all)** : CRUD Rest API, API documentation, Audit Rest API with custom RevisionEntity and CRUD Web application generation

Build the samples
=================
Run **``mvn clean install``** at the root of the ``sample-app`` folder to build all samples.

Run the samples
=============== 
The easiest way for running the samples is to go in the sample project root folder and execute the sample as a standard Spring Boot application using **``mvn spring-boot:run``**.

> **Note** : For running the [*petclinic-web*](petclinic-web) sample you will have to execute the following comands : 
> 
> - ``mvn spring-boot:run`` for starting the API (at port 8080 by default)
> - ``mvn spring-boot:run -Dserver.port=<FREE_PORT>`` for starting the Web Admin UI where FREE_PORT has to be a free port (different from the one used by the API)

However you might prefer deploy the WARs in a Tomcat instance (or another servlet container) : that is naturally feasible.
