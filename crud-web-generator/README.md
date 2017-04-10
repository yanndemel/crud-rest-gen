# crud-web-generator :  CRUD Web application generation framework

Used by **crud-maven-plugin:crudweb** to generate the **Angular JS Admin CRUD application** for administrating a JPA data model using the generated Rest API as backend (generated with *crud-maven-plugin:crudapi*).

Project Dependencies
====================
* *Hibernate entity manager dependencies* : to instantiate the EntityManagerFactory (given the name of the persistence-unit)
* *Apache Velocity* : to use Velocity as template engine for generating the sources
* *H2 database* : to use H2 as default database (must match the db defined in the persistence.xml file of your data model Jar)
* [*crud-generator-utils*](../crud-generator-utils) : to use the utility classes in the package *com.octo.tools.crud.utils* (StringUtils & ReflectionUtils)

Resources files
===============
* [*Static data*](src/main/resources/static) : will be copied as-is to the root of the target generation folder
 * /css : bootstrap 3 CSS files and crud-admin CSS
 * /fonts : glyphicons needed by bootstrap
 * /img : Angular JS logo
 * /js : transverse JS modules used by the generated JS files
 * /libs : Angular JS JS dependencies
 * /partials : welcome.html page
* [*Templates*](src/main/resources/templates) : templates used by the Velocity engine to generate the Web application files
 * /js : Velocity templates for JS generation
 * /page : Velocity templates for HTML generation

Java code
=========
The main class is [**CrudGenrator**](src/main/java/com/octo/tools/crudweb/CrudGenerator.java). *crud-maven-plugin* calls the *generate* method with the following arguments :

* *persistenceUnitName* : name of the persistence unit defined in the persistence.xml of the data model
* *destDirRelativePath* : path of the generation folder
* *restUrl* : Root URL of the Rest API backend (assuming the API is generated with *crud-maven-plugin:crudapi*)

The 2 other classes ([*FieldInfo*](/src/main/java/com/octo/tools/crudweb/FieldInfo.java) and [*FieldUtils*](src/main/java/com/octo/tools/crudweb/FileUtils.java)) are utility classes used by CrudGenerator.

How it works ?
==============
CrudGenerator introspects the data model to retrieve persistence information, then :

* Copy static resources to the generation folder
* Generates app.js and services.js code (based on their corresponding *.vm templates)

For each JPA entity, it generates :

* 2 JS files in ``js/<entity>/`` :
 * ``<entity>_controller.js``
 * ``<entity>_module.js``
* 2 HTML files in ``partials/<entity>/`` :
 * ``<entity>_form.html``
 * ``<entity>_list.html``

Finally it generates the index.html file based on the corresponding index_html.vm template.

