# **petclinic-model** : Base data model

The original [Spring Petclinc](https://github.com/spring-projects/spring-petclinic) model has been slightly adapted to suit crud-maven-plugin prerequisites :

Prerequisite #1 : Your entities must be POJOs annotated with standard **javax.persistence annotations** : currently only **Field access** defined annotations are supported
================
Standard javax.persistence annotations are already used in the Petclinic model.
For the purpose of generating the standard CRUD API documentation I have added *@JsonIgnore* annotations on the methods that we don't want to be exposed : BaseEntity.isNew() and Vet.getNrOfSpecialties() (see [petclinic-api-doc](https://github.com/yanndemel/crud-rest-gen/tree/master/sample-app/petclinic-api-doc) for details).
Finally to avoid Date serialization problems I have added *@JsonFormat* annotations on the date fields in Pet.birthDate and Visit.date.



Prerequisite #2 : "The type of all @Id in your entities must be java.lang.Long"
================
The BaseEntity has been modified : the type of "id" field becomes Long instead of Integer (getter and setter have been updated as well).

