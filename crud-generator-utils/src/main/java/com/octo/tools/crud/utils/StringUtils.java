package com.octo.tools.crud.utils;

import java.text.Normalizer;

import org.atteo.evo.inflector.English;

/**
 * Helper class for String operations
 * 
 * @author OCTO
 * 
 */
public class StringUtils {

	public static final String VALUE_SEP = ",";

	private StringUtils() {};
	
	/**
	 * @param o the object
	 * @return null if o is null, else o.toString()
	 */
	public static String toString(Object o) {
		return o== null ? null : o.toString();
	}
	
	/**
	 * @param o the object
	 * @return null if o is null, else o.toString()
	 */
	public static String getId(Object o) {
		return o== null ? null : o.toString();
	}	
	
	
	/**
	 * @param name the name (singular form)
	 * @return the plural form of the name
	 */
	public static String plural(String name) {
		if(name == null || name.length() == 0) {
			return name;
		}
		return English.plural(name);
	}

	public static String removeAccents(String name) {
		String normalize = Normalizer.normalize(name, Normalizer.Form.NFD);
		return normalize.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	public static String concatFirstUpper(String lastName, String firstName) {
		StringBuilder sb = new StringBuilder();
		if(lastName != null)			
			sb.append(lastName.toUpperCase());
		if(firstName != null) {
			if(sb.length() > 0)
				sb.append(" ");
			sb.append(firstName);
		}		
		return sb.toString();
	}
	
	public static boolean isUpperCase(String s) {
		for (int i=0; i<s.length(); i++)
	    {
	        if (Character.isLowerCase(s.charAt(i)))
	        {
	            return false;
	        }
	    }
	    return true;
	}

	public static String extractFirstValue(String values) {
		if(values == null)
			return null;
		int i = values.indexOf(VALUE_SEP);
		if(i > 0) {
			return values.substring(0, i);
		}
		return values;
	}
	
	
	
}
