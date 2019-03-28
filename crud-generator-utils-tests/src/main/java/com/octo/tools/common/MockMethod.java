package com.octo.tools.common;

import java.lang.reflect.Method;

public class MockMethod {
	private Method method;
	private Object instance;
	
	public MockMethod(Method method, Object instance) {
		super();
		this.method = method;
		this.instance = instance;
	}
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	public Object getInstance() {
		return instance;
	}
	public void setInstance(Object instance) {
		this.instance = instance;
	}
	
}
