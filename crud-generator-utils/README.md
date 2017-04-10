# crud-generator-utils : Utility classes

Used as a dependency by *crud-maven-plugin* and dependencies.
Can be use as a dependency in client projects for using default controllers, filter or helper classes.

Java Code
=========
Default controllers can be used in client projects for :

* Redirecting to the generated Admin Web main page : [AdminController](src/main/java/com/octo/tools/crud/admin/AdminController.java)
* Redirecting to the generated API Documentation page : [ApiDocsController](src/main/java/com/octo/tools/crud/doc/ApiDocsController.java)
* Adding /doc at the root of the Rest API : [DocResourceProcessor](src/main/java/com/octo/tools/crud/doc/DocResourceProcessor.java)
* Enabling requests from any host : [SimpleCORSFilter](src/main/java/com/octo/tools/crud/filter/SimpleCORSFilter.java)
* Helper classes for [Reflection](src/main/java/com/octo/tools/crud/utils/ReflectionUtils.java) and [String](src/main/java/com/octo/tools/crud/utils/StringUtils.java) operations

Sample usages of this classes can be found in the [samples](../sample-app).
