package com.octo.tools.crud.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.envers.RevisionEntity;

/**
 * Helper class for reflection operations
 * 
 * @author OCTO
 * 
 */
public class ReflectionUtils {

	private ReflectionUtils() {}
	
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		if (type.isAnnotationPresent(Entity.class) || type.isAnnotationPresent(MappedSuperclass.class)) {
			fields.addAll(Arrays.asList(type.getDeclaredFields()));			
			if (type.getSuperclass() != null) {
				return getAllFields(fields, type.getSuperclass());
			}
		}
	
		return fields;
	}

	
	/**
	 * @param type class to introspect
	 * @return the list of all fields of this class (including fields of superclasses)
	 */
	public static List<Field> getAllFields(Class<?> type) {
		return getAllFields(new ArrayList<Field>(), type);
	}	
	
	/**
	 * @param javaType entity class
	 * @return true if javaType is not annotated with {@link org.hibernate.envers.RevisionEntity}
	 */
	public static boolean isEntityExposed(Class<?> javaType) {
		return javaType != null && !javaType.isAnnotationPresent(RevisionEntity.class)
				&& !javaType.equals(DefaultRevisionEntity.class);
	}

	/**
	 * @param pd PropertyDescriptor
	 * @param f field linked to pd
	 * @return SimpleName of return type for non collection fields, else &lt;Generic type of collection&gt; 
	 */
	public static String getReturnTypeAsString(PropertyDescriptor pd, Field f) {
		String simpleName = pd.getReadMethod().getReturnType().getName();
		if(Collection.class.isAssignableFrom(pd.getPropertyType())) {
			ParameterizedType genericType = (ParameterizedType) f.getGenericType();
			if(genericType != null) {
				StringBuilder sb = new StringBuilder(simpleName + "<");
				for(Type t : genericType.getActualTypeArguments()) {
					sb.append(t.getTypeName()).append(">");
				}
				return sb.toString(); 
			}	
		}
		return simpleName;
	}
	
	

	/**
	 * @param pds array of the {@link PropertyDescriptor} of the class
	 * @param f field to get PropertyDescriptor
	 * @return the {@link PropertyDescriptor} of f, null if not found
	 */
	public static PropertyDescriptor getPropertyDescriptor(
			PropertyDescriptor[] pds, Field f)  {
		for(PropertyDescriptor p : pds) {
			if(f.getName().equals(p.getName())) {
				return p;
			}
		}
		return null;
	}
	
	
	/**
	 * @param c Instance class
	 * @return true if c is a {@link Number} subclass (or primitive number type)
	 */
	public static boolean isNumber(Class<?> c) {
		if(Number.class.isAssignableFrom(c) || 
				(c.isPrimitive() && isPrimitiveNumber(c)))
			return true;
		return false;
	}

	private static boolean isPrimitiveNumber(Class<?> c) {
		return c.equals(int.class) || c.equals(double.class) || c.equals(long.class) || c.equals(short.class);
	}

	/**
	 * @param fieldType Instance class
	 * @return true if fieldType is {@link Boolean} or boolean primitive type
	 */
	public static boolean isBoolean(Class<?> fieldType) {
		return fieldType.equals(Boolean.class) || fieldType.equals(boolean.class);
	}


	/**
	 * @param field Field to test
	 * @return true if field is annotated with {@link OneToMany} or {@link ManyToMany}
	 */
	public static boolean hasCollections(Field field) {
		return field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)
				|| field.isAnnotationPresent(ElementCollection.class);
	}

	

	
	/**
	 * Get the simple name of the class T when a field is a Collection<T>
	 * @param f the field holding the Collection
	 * @return the simple name of the first parameterized type of the collection, null if f is not a generic parameterized type
	 * @throws ClassNotFoundException if the parameterized type is not found in the classpath
	 */
	public static String getGenericCollectionTypeName(Field f) throws ClassNotFoundException {
		ParameterizedType genericType = (ParameterizedType) f.getGenericType();
		if(genericType != null) {
			for(Type t : genericType.getActualTypeArguments()) {
				return Class.forName(t.getTypeName()).getSimpleName();
			}
		}
		return null;
	}
	
	/**
	 * Get the simple name of the class T when a field is a Collection<T>
	 * @param f the field holding the Collection
	 * @return the class of the first parameterized type of the collection, null if f is not a generic parameterized type
	 * @throws ClassNotFoundException if the parameterized type is not found in the classpath
	 */
	public static Class<?> getGenericCollectionType(Field f) throws ClassNotFoundException {
		ParameterizedType genericType;
		try {
			genericType = (ParameterizedType) f.getGenericType();
		} catch (Exception e) {
			return null;
		}
		if(genericType != null) {
			for(Type t : genericType.getActualTypeArguments()) {
				return Class.forName(t.getTypeName());
			}
		}
		return null;
	}
	
	public static String[] getNames(Class<? extends Enum<?>> e) {
	    return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
	}

	public static  boolean isSingleTableInheritance(Class<?> javaType) {		
		return javaType.isAnnotationPresent(Inheritance.class) && javaType.isAnnotationPresent(DiscriminatorColumn.class);
	}

	public static String getIdClass(Class<?> javaType) {
		for (Field f : getAllFields(javaType)) {
			if (f.isAnnotationPresent(Id.class)) {
				return f.getType().getSimpleName();
			}
		}
		return null;
	}

    public static boolean isAudited(Class<?> entityClass) {
        return entityClass != null && entityClass.isAnnotationPresent(Audited.class)
                && entityClass.getAnnotation(Audited.class).targetAuditMode().equals(RelationTargetAuditMode.AUDITED)
                && isEntityExposed(entityClass);
    }
}
