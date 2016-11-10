#audit-core : Base classes for Audit Rest API

Project used by the crud-maven-plugin and as dependency of client projects for audit Rest API generation.

* [Base controller](src\main\java\com\octo\tools\audit\AuditControllerBase.java) mapping the /history resource
* Abstract controllers for processing audit information :
 * [AbstractAuditController](src/main/java/com/octo/tools/audit/AbstractAuditController.java)
 * [AbstractDefaultAuditController](src/main/java/com/octo/tools/audit/AbstractDefaultAuditController.java)
 * [AbstractReflectionAuditController](src/main/java/com/octo/tools/audit/AbstractReflectionAuditController.java)
* [Audit resouce processor](src/main/java/com/octo/tools/audit/AuditResourceProcessor.java) and [default audit resouce support](src/main/java/com/octo/tools/audit/AuditResourceSupport.java)
* [Audit configuration exception](src/main/java/com/octo/tools/audit/AuditConfigurationException.java)

Sample usages of this project can be found in the [samples](../sample-app).
