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

	private static final String D = "d'";
	private static final String DES = "des";
	private static final String DU = "du";
	private static final String DE = "de";
	public static final String SPACE = " ";
	public static final String HYPHEN = "-";
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
		return normalize.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.replaceAll("(\\\\|/|\\.)", SPACE).trim();
	}

	public static String concatFirstUpper(String lastName, String firstName) {
		StringBuilder sb = new StringBuilder();
		if(lastName != null)			
			sb.append(lastName.toUpperCase());
		if(firstName != null) {
			if(sb.length() > 0)
				sb.append(SPACE);
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

	public static String doubleToString(Double price) {		
		return price % 1 == 0 ? Integer.toString(price.intValue()) : price.toString();
	}

	public static String formatLongName(String firstName, String lastName) {
		StringBuilder sb = new StringBuilder();
		if(firstName != null && firstName.length() > 0) {			
			appendFirstLetterCapitalized(firstName, sb, false);
			sb.append(SPACE);
		}
		if(lastName != null && lastName.length() > 0) {
			appendFirstLetterCapitalized(lastName, sb, true);
		}
		return sb.toString();
	}

	private static void appendFirstLetterCapitalized(String name, StringBuilder sb, boolean checkParticule) {
		String[] parts = name.split(SPACE);
		for(int i = 0; i<parts.length; i++) {
			if(parts[i].contains(HYPHEN)) {
				String[] pp = parts[i].split(HYPHEN);
				for(int j = 0; j<pp.length; j++) {
					sb.append(capitalizeFirstLetter(pp[j].toLowerCase(), checkParticule));
					if(j<pp.length - 1) {
						sb.append(HYPHEN);
					}
				}
			} else {
				sb.append(capitalizeFirstLetter(parts[i].toLowerCase(), checkParticule));				
			}
			if(i<parts.length - 1) {
				sb.append(SPACE);
			}
		}
	}

	public static String capitalizeFirstLetter(String s, boolean checkParticule) {
		if(s == null) {
			return null;
		}
		int len = s.length();
		if(len > 1) {
			if(checkParticule) {
				switch(s) {
				case DE:
				case DU:
				case DES:
					return s;
				default:
					break;
				}				
				if(s.startsWith(D)) {
					return len > 2 ? D + capitalizeFirstLetter(s.substring(2), false) : D;
				}
			}			
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}
		return s.toUpperCase();
	}
	 
	public static boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
}
