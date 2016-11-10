#petclinic-audit-default : Base class extended by the generated audit controllers

The only class in this project is [**AbstractAuditController**](src/main/java/com/octo/tools/samples/AbstractAuditController.java) which extends the default abstract audit controller used by the *crud-maven-plugin:audit* for generating the audit controllers.

This class extends the default [AbstractDefaultAuditController](../../audit-core/src/main/java/com/octo/tools/audit/AbstractDefaultAuditController.java) and overrides the ``protected Long getEntityId(T entity)`` method (in order to avoid reflection calls for retriving the id of the entities).
