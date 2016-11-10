#petclinic-audit-model-common : PetClinic model with audit information

The project contains **only java classes. No persistence.xml is defined here**.

The same modifications have been done as in project [petclinic-model](../petclinic-model).

Moreover **org.hibernate.envers.Audited** annotation has been added on all JPA entities (including @MappedSuperClass classes).
