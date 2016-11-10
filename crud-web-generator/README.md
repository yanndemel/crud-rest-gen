#crud-web-generator :  CRUD Web application generation framework

Used by *crud-maven-plugin* to generate the **Angular JS Admin CRUD application** for administrating a JPA data model using the generated Rest API as backend (generated with *crud-maven-plugin:crudapi*).

Project Dependencies
====================
* *Hibernate entity manager dependencies* : to instantiate the EntityManagerFactory (given the name of the persistence-unit)
* *Apache Velocity* : to use Velocity as template engine for generating the sources
* *H2 database* : to use H2 as default database (must match the db defined in the persistence.xml file of your data model Jar)
* [*crud-generator-utils*](../crud-generator-utils) : to use the utility classes in the package *com.octo.tools.crud.utils* (StringUtils & ReflectionUtils)

Resources files
===============


Java code
=========

How it works ?
==============
