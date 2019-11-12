package com.octo.tools.samples.petclinic;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.octo.tools.crud.doc.BaseApiDocumentation;

@ContextConfiguration(classes = Application.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class PetClinicBaseApiDocumentation extends BaseApiDocumentation {


	
	
}
