package com.octo.tools.crud.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EntityInfo {

	public Class entityClass;
	private String simpleName1stUpper;
	private String simpleName;
	private String pluralName;
	private String pluralName1stUpper;
	private boolean search;
	private boolean paged;
	private List<Map<String, String>> dataSet;
	private Map<String, String> currentElement;
	private Iterator<Map<String, String>> dataSetIterator;
	private List<String> allFields;
	
	public String getSimpleName1stUpper() {
		return simpleName1stUpper;
	}
	public void setSimpleName1stUpper(String simpleName1stUpper) {
		this.simpleName1stUpper = simpleName1stUpper;
	}
	public String getSimpleName() {
		return simpleName;
	}
	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}
	public String getPluralName() {
		return pluralName;
	}
	public void setPluralName(String pluralName) {
		this.pluralName = pluralName;
	}
	public String getPluralName1stUpper() {
		return pluralName1stUpper;
	}
	public void setPluralName1stUpper(String pluralName1stUpper) {
		this.pluralName1stUpper = pluralName1stUpper;
	}
	public Class getEntityClass() {
		return entityClass;
	}
	public void setEntityClass(Class entityName) {
		this.entityClass = entityName;
	}
	@Override
	public String toString() {
		return "EntityInfo [entityClass=" + entityClass + ", simpleName1stUpper=" + simpleName1stUpper + ", simpleName="
				+ simpleName + ", pluralName=" + pluralName + ", pluralName1stUpper=" + pluralName1stUpper + "]";
	}
	public boolean isSearch() {
		return search;
	}
	public void setSearch(boolean hasSearch) {
		this.search = hasSearch;
	}
	public boolean isPaged() {
		return paged;
	}
	public void setPaged(boolean paged) {
		this.paged = paged;
	}
	public List<Map<String, String>> getDataSet() {
		return dataSet;
	}
	public void setDataSet(List<Map<String, String>> dataSet) {
		this.dataSet = dataSet;	
		this.dataSetIterator = this.dataSet.iterator();
		this.allFields = new ArrayList<String>();
	}
	public String getNextValue(String name) {
		if(allFields.contains(name)) {
			if(!this.dataSetIterator.hasNext())
				this.dataSetIterator = this.dataSet.iterator();
			allFields.clear();
			this.currentElement = this.dataSetIterator.next();
		}
		allFields.add(name);
		if(this.currentElement == null)
			this.currentElement = this.dataSetIterator.next();
		return this.currentElement.get(name);
	}
	
	
	
}
