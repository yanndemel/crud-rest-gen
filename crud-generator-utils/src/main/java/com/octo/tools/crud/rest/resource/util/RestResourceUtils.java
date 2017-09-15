package com.octo.tools.crud.rest.resource.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.naming.ConfigurationException;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.octo.tools.crud.rest.resource.RestRemoteResource;
import com.octo.tools.crud.utils.ReflectionUtils;

public class RestResourceUtils {

	private static final Logger logger = LoggerFactory.getLogger(RestResourceUtils.class);
	
	private static final String ERROR_MSG = "Configuration problem for field %s : Annotation field 'resolveToPreperty' must reference a @Transient field of type RestRemoteResource or Collection<RestRemoteResource>";

	public static Method getWriteMethod(List<Field> fields, PropertyDescriptor[] pds, String fieldName)
			throws ConfigurationException {
		for (Field f : fields) {
			if (f.getName().equals(fieldName)) {
				if (!isRemoteResourceField(f)) {
					throw new ConfigurationException(String.format(ERROR_MSG, f.getName()));
				}
				PropertyDescriptor pd = ReflectionUtils.getPropertyDescriptor(pds, f);
				if (pd != null && pd.getWriteMethod() != null) {
					return pd.getWriteMethod();
				}
			}
		}
		return null;
	}

	public static boolean isRemoteResourceField(Field f) {
		try {
			return (f.getType().equals(RestRemoteResource.class) 
					|| (Collection.class.isAssignableFrom(f.getType())
					&& RestRemoteResource.class.equals(ReflectionUtils.getGenericCollectionType(f))))
				&& f.isAnnotationPresent(Transient.class);
		} catch (ClassNotFoundException e) {
			logger.error("Exception", e);
			return false;
		}
	}

}
