package com.octo.tools.samples.petclinic;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.octo.tools.crud.doc.ADocEntityGenerator;

@ContextConfiguration(classes = Application.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class PetClinicADocEntityGenerator extends ADocEntityGenerator {

		
}
