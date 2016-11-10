#crud-maven-plugin : Maven plugin for Rest API, Audit Rest API and CRUD administration Web application generation

Build the plugin
================
Execute ``mvn clean install`` to build the plugin.

To generate the plugin documentation execute ``mvn site`` : it will generate the documentation in target/site/plugin-info.html

Plugin dependencies
===================
In addition to the classical dependencies needed by the maven plugins, *crud-maven-plugin* depends on :

* [crud-web-generator](../crud-web-generator)
* [crud-generator-utils](../crud-generator-utils)
* [audit-core](../audit-core)

Java code
=========
3 Mojos compose the plugin :

* AuditGeneratorMojo for Audit Rest API generation (**audit** goal)
* CrudApiGeneratorMojo for CRUD Rest API generation (**crudapi** goal)
* CrudWebGeneratorMojo for CRUD administration Web application (**crudweb** goal)

Sample usages of the plugin can be found in the [samples](../sample-app).



