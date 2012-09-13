package org.ironrhino.core.model;

import org.ironrhino.core.search.elasticsearch.annotations.Index;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;

@SuppressWarnings("rawtypes")
public interface Ordered extends Comparable {

	@SearchableProperty(index = Index.NOT_ANALYZED)
	public int getDisplayOrder();
}
