package com.octo.tools.crud.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringTest {

	@Test
	public void testFormatLongName() {
		String formatLongName = StringUtils.formatLongName("jean-BAPTISTE NOël", "De la ROCHEDUBOIS DES entrailles "
				+ "De la TERRE D'espérance DU con lajoie");
		assertTrue(formatLongName, "Jean-Baptiste Noël de La Rochedubois des Entrailles de La Terre d'Espérance du Con Lajoie"
				.equals(formatLongName));
		assertEquals("Jean-Baptiste", StringUtils.toTitleCase("JEAN-BAPTIste"));
		assertEquals("Jean Noël", StringUtils.toTitleCase("JEAN NOËL"));
	}
	
	
	@Test
	public void testNormalize() {
		String s = "Hello  Mr Vincêt \n coco titi\r héhö";
		System.out.println(s);
		s = StringUtils.removeCarriageReturnsAndAccents(s);
		System.out.println(s);
		
	}

	@Test
	public void testAnnotations() {
		boolean audited = ReflectionUtils.isAudited(Mailbox.class);
		assertFalse(audited);
	}
	
	
}
