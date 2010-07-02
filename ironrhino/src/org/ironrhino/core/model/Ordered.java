package org.ironrhino.core.model;

import org.compass.annotations.Index;
import org.compass.annotations.SearchableProperty;

public interface Ordered extends Comparable {

	@SearchableProperty(index = Index.NOT_ANALYZED)
	public int getDisplayOrder();
}
