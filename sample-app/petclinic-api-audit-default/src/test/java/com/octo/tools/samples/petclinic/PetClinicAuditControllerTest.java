package com.octo.tools.samples.petclinic;

import org.springframework.samples.petclinic.Application;
import org.springframework.test.context.ContextConfiguration;

import com.octo.tools.audit.AuditControllersTest;

@ContextConfiguration(classes = Application.class)
public class PetClinicAuditControllerTest extends AuditControllersTest {

}
