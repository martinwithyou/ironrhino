package org.ironrhino.core.model;

import org.compass.annotations.Index;
import org.compass.annotations.SearchableProperty;

@SuppressWarnings("rawtypes")
public interface Ordered extends Comparable {

	@SearchableProperty(index = Index.NOT_ANALYZED)
	public int getDisplayOrder();
}
