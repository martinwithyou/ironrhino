package org.ironrhino.core.search;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.compass.core.CompassQuery.SortPropertyType;

public class CompassCriteria {

	private Set<CompassSort> sorts = new LinkedHashSet<CompassSort>(0);

	private Set<CompassCondition> conditions = new HashSet<CompassCondition>(0);

	private String[] highlightFields;

	private String query;

	private int pageNo = 1;

	private int pageSize = 20;

	private String[] aliases;

	private Float boost;

	public void addSort(String sortParamName, String paramType,
			boolean isDescend) {
		SortPropertyType type;
		if ("STRING".equalsIgnoreCase(paramType))
			type = SortPropertyType.STRING;
		else if ("INT".equalsIgnoreCase(paramType))
			type = SortPropertyType.INT;
		else if ("FLOAT".equalsIgnoreCase(paramType))
			type = SortPropertyType.FLOAT;
		else
			type = SortPropertyType.AUTO;
		this.sorts.add(new CompassSort(sortParamName, type, isDescend));
	}

	public Set<CompassSort> getSorts() {
		return sorts;
	}

	public Set<CompassCondition> getConditions() {
		return conditions;
	}

	public void between(String name, Object low, Object high) {
		this.conditions.add(new CompassCondition(name, low, high));
	}

	public void ge(String name, Object value) {
		this.conditions.add(new CompassCondition(name, value,
				CompassConditionType.GE));
	}

	public void gt(String name, Object value) {
		this.conditions.add(new CompassCondition(name, value,
				CompassConditionType.GT));
	}

	public void le(String name, Object value) {
		this.conditions.add(new CompassCondition(name, value,
				CompassConditionType.LE));
	}

	public void lt(String name, Object value) {
		this.conditions.add(new CompassCondition(name, value,
				CompassConditionType.LT));
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String[] getHighlightFields() {
		return highlightFields;
	}

	public void setHighlightFields(String[] highlightFields) {
		this.highlightFields = highlightFields;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	public Float getBoost() {
		return boost;
	}

	public void setBoost(Float boost) {
		this.boost = boost;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
