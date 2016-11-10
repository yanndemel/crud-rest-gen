#petclinic-audit-model-custom : PetClinic model with audit (custom Hibernate Envers configuration)

This project contains :
* the [*persistence.xml*](src/main/resources/META-INF/persistence.xml) file
* the custom revision entity used ([CustomRevisionEntity](src/main/java/org/springframework/samples/petclinic/model/CustomRevisionEntity.java)
* the custom revision listener allowing to retrieve additional information at each revision ([CustomRevisionEntityListener](src/main/java/org/springframework/samples/petclinic/model/CustomRevisionEntityListener.java))

All other referenced JPA entities are defined in the dependent project [*petclinic-audit-model-common*](../petclinic-audit-model-common).
