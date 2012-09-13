package org.ironrhino.core.search.elasticsearch;

import org.ironrhino.core.search.SearchCriteria;

public class ElasticSearchCriteria extends SearchCriteria {

	private static final long serialVersionUID = 2810417180615970724L;

	private String[] indices;

	private String[] types;

	private Float boost;

	public String[] getIndices() {
		return indices;
	}

	public void setIndices(String[] indices) {
		this.indices = indices;
	}

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}

	public Float getBoost() {
		return boost;
	}

	public void setBoost(Float boost) {
		this.boost = boost;
	}

}
