package com.octo.tools.crud.util;

import java.util.HashMap;
import java.util.Map;

public class EntityInfo {

	public Class entityClass;
	private String simpleName1stUpper;
	private String simpleName;
	private String pluralName;
	private String pluralName1stUpper;
	private boolean search;
	private boolean paged;
	private boolean hasOnlyManyToOne;
	private Map<String, String> dataSet;
	private Map<String, String> updateDataSet;
	private Map<String, String> overrideDataSet;
	private Map<String, String> updateOverrideDataSet;
	
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

	public Map<String, String> getDataSet() {
		return dataSet;
	}

	public void setDataSet(Map<String, String> dataSet) {
		this.dataSet = dataSet;
	}

	public String getValue(String fieldName, boolean forUpdate) {
		if (forUpdate && updateDataSet != null && updateDataSet.containsKey(fieldName))
			return updateDataSet != null ? updateDataSet.get(fieldName) : null;		
		return dataSet.get(fieldName);
	}
	
	public String getOverrideValue(String fieldName, boolean forUpdate) {
		if (forUpdate)
			return updateOverrideDataSet != null ? updateOverrideDataSet.get(fieldName) : null;		
		return overrideDataSet != null ? overrideDataSet.get(fieldName) : null;
	}
	
	public void reset() {
		if(updateOverrideDataSet != null)
			updateOverrideDataSet.clear();
		if(overrideDataSet != null)
			overrideDataSet.clear();
	}
	
	public void setOverrideValue(String fieldName, boolean forUpdate, String value) {
		if (forUpdate) {
			if(updateOverrideDataSet == null)
				updateOverrideDataSet = new HashMap<String, String>();
			updateOverrideDataSet.put(fieldName, value);		
		}
		else {
			if(overrideDataSet == null)
				overrideDataSet = new HashMap<String, String>();
			overrideDataSet.put(fieldName, value);
		}
	}

	public Map<String, String> getUpdateDataSet() {
		return updateDataSet;
	}

	public void setUpdateDataSet(Map<String, String> updateDataSet) {
		this.updateDataSet = updateDataSet;
	}

	public boolean hasOnlyManyToOne() {
		return hasOnlyManyToOne;
	}

	public void setHasOnlyManyToOne(boolean hasOnlyManyToOne) {
		this.hasOnlyManyToOne = hasOnlyManyToOne;
	}
	
	

}
