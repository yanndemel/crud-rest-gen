#petclinic-audit-custom : Base class extended by the generated audit controllers for customized audited model

This project is used by [../petclinic-api-audit-custom].

Java code
=========
The 2 classes in this project are :

* [**AbstractAuditController**](src/main/java/com/octo/tools/samples/AbstractAuditController.java) which extends the default abstract audit controller used by the *crud-maven-plugin:audit* for generating the audit controllers. This class extends the default [AbstractAuditController<T, CustomRevisionEntity>](../../audit-core/src/main/java/com/octo/tools/audit/AbstractAuditController.java) and overrides the following methods :
 * ``protected AuditResourceSupport<T> newAuditResourceSupport(RevisionType revType, T entity, CustomRevisionEntity revEntity)``
 * ``protected Long getRevisionEntityId(CustomRevisionEntity revEntity)``
 * ``protected Long getRevisionEntityTimestamp(CustomRevisionEntity revEntity)``
 * ``protected Long getEntityId(T entity)`` in order to avoid reflection calls for retriving the id of the entities
* [**AuditResourceSupport<T extends BaseEntity>**](src/main/java/com/octo/tools/samples/AuditResourceSupport.java) in order to add custom information (user name in the sample) to the responses returned by the audit API.

Dependencies
============
The project depends on :
* [*audit-core*](../../audit-core) : base controllers and resource support classes
* [*petclinic-audit-model-custom*](../petclinic-audit-model-custom) : JPA entities

