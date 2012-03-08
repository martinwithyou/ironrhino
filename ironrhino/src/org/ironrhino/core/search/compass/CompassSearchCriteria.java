package org.ironrhino.core.search.compass;

import org.compass.core.CompassQueryFilter;
import org.ironrhino.core.search.SearchCriteria;

public class CompassSearchCriteria extends SearchCriteria {

	private static final long serialVersionUID = 8069132090861765822L;

	private String[] aliases;

	private Float boost;

	private CompassQueryFilter filter;

	public CompassQueryFilter getFilter() {
		return filter;
	}

	public void setFilter(CompassQueryFilter filter) {
		this.filter = filter;
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

}
