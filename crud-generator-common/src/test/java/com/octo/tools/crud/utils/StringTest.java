package com.octo.tools.crud.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
	
	
}
