package com.octo.tools.crud.rest.resource.util;

import java.lang.reflect.Method;
import java.util.Collection;

import com.octo.tools.crud.rest.annotation.RestResourceMapper;

public class FieldInfo {

	String fieldName;
	RestResourceMapper annotation;
	Method fieldGetter;
	Method propertySetter;
	private boolean collection;
	
	public FieldInfo(String fieldName, RestResourceMapper annotation, Method fieldGetter, Method propertySetter) {
		super();
		this.fieldName = fieldName;
		this.annotation = annotation;
		this.fieldGetter = fieldGetter;
		if(Collection.class.isAssignableFrom(this.fieldGetter.getReturnType()))
			this.collection = true;
		else
			this.collection = false;
		this.propertySetter = propertySetter;
	}

	public String getFieldName() {
		return fieldName;
	}

	public RestResourceMapper getAnnotation() {
		return annotation;
	}

	public Method getFieldGetter() {
		return fieldGetter;
	}

	public Method getPropertySetter() {
		return propertySetter;
	}

	public boolean isCollection() {
		return collection;
	}
	
	
	
}
